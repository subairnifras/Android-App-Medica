package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var isFirebaseInitialized = false

    // Firebase instances
    private var authInstance: FirebaseAuth? = null
    private var firestoreInstance: FirebaseFirestore? = null

    // Fallback/Simulated Data Engine
    private val _currentUserFlow = MutableStateFlow<UserProfile?>(null)
    val currentUserFlow: StateFlow<UserProfile?> = _currentUserFlow.asStateFlow()

    private val _appointmentsFlow = MutableStateFlow<List<Appointment>>(emptyList())
    val appointmentsFlow: StateFlow<List<Appointment>> = _appointmentsFlow.asStateFlow()

    private val _chatsFlow = MutableStateFlow<List<Chat>>(emptyList())
    val chatsFlow: StateFlow<List<Chat>> = _chatsFlow.asStateFlow()

    // Map of chatId -> Flow of messages
    private val messagesMap = mutableMapOf<String, MutableStateFlow<List<Message>>>()

    val doctorsList = listOf(
        Doctor(
            id = "doc1",
            name = "Dr. Daniel Michael",
            specialty = "Neurologist",
            image = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?q=80&w=200&auto=format&fit=crop",
            rating = 4.8,
            reviewsCount = 4821,
            experienceYears = 12,
            patientsCount = 6200,
            fee = 25.0,
            about = "Dr. Daniel Michael is a highly experienced Neurologist specialized in diagnosing and treating diseases of the brain, spinal cord, nerves, and muscles. He has been in practice for over 12 years with a outstanding track record.",
            workingHours = "Monday - Friday: 8:00 PM - 10:00 PM"
        ),
        Doctor(
            id = "doc2",
            name = "Dr. Mohamed Zahran",
            specialty = "Cardiologist",
            image = "https://images.unsplash.com/photo-1537368910025-700350fe46c7?q=80&w=200&auto=format&fit=crop",
            rating = 4.9,
            reviewsCount = 3120,
            experienceYears = 15,
            patientsCount = 8400,
            fee = 29.0,
            about = "Dr. Mohamed Zahran offers world-class cardiovascular diagnostics, treatment, and preventive healthcare services. He dedicates his career to modern and clinical care operations.",
            workingHours = "Monday - Thursday: 7:00 PM - 9:30 PM"
        ),
        Doctor(
            id = "doc3",
            name = "Dr. Fazira Shafi",
            specialty = "Dentist",
            image = "https://images.unsplash.com/photo-1594824813573-246434de83fb?q=80&w=200&auto=format&fit=crop",
            rating = 4.7,
            reviewsCount = 1890,
            experienceYears = 8,
            patientsCount = 3800,
            fee = 19.0,
            about = "Dr. Fazira Shafi is passionate about oral health, smile reconstructions, and preventive dentistry. She is welcoming, caring, and loves helping all age groups.",
            workingHours = "Tuesday - Saturday: 9:00 AM - 1:00 PM"
        ),
        Doctor(
            id = "doc4",
            name = "Dr. Arjuna Raman",
            specialty = "Dentist",
            image = "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=200&auto=format&fit=crop",
            rating = 4.6,
            reviewsCount = 980,
            experienceYears = 6,
            patientsCount = 2200,
            fee = 22.0,
            about = "Dr. Arjuna Raman is skilled in orthodontist procedures and standard cosmetic treatments. Friendly approach ensures immediate comfort for anxious patients.",
            workingHours = "Wednesday - Friday: 4:00 PM - 7:00 PM"
        )
    )

    init {
        // Initialize simulated default data
        val defaultChats = listOf(
            Chat(
                id = "chat_doc1",
                doctorId = "doc1",
                doctorName = "Dr. Daniel Michael",
                doctorImage = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?q=80&w=200&auto=format&fit=crop",
                lastMessage = "Hey! What about your health?",
                lastMessageTime = "5min ago"
            ),
            Chat(
                id = "chat_doc2",
                doctorId = "doc2",
                doctorName = "Dr. Mohamed Zahran",
                doctorImage = "https://images.unsplash.com/photo-1537368910025-700350fe46c7?q=80&w=200&auto=format&fit=crop",
                lastMessage = "Sure, I'll update you on...",
                lastMessageTime = "5min ago"
            )
        )
        _chatsFlow.value = defaultChats

        messagesMap["chat_doc1"] = MutableStateFlow(listOf(
            Message("m1", "doctor", "Good afternoon, What's your problem?", System.currentTimeMillis() - 10000000L),
            Message("m2", "user", "I've been having headache and cold for 5 days. I took 2 Dolo tablets, but the pain is still there", System.currentTimeMillis() - 8000000L),
            Message("m3", "doctor", "Are you experiencing fever as well? How severe is your headache?", System.currentTimeMillis() - 5000000L),
            Message("m4", "user", "I took 2 Dolo tablets, but the pain is still there", System.currentTimeMillis() - 2000000L),
            Message("m5", "doctor", "Hey! What about your health?", System.currentTimeMillis() - 300000L)
        ))

        messagesMap["chat_doc2"] = MutableStateFlow(listOf(
            Message("z1", "doctor", "Good afternoon! Please share your latest test records.", System.currentTimeMillis() - 5000000L),
            Message("z2", "user", "I will upload my blood pressure details", System.currentTimeMillis() - 2000000L),
            Message("z3", "doctor", "Sure, I'll update you on the dosage.", System.currentTimeMillis() - 300000L)
        ))
    }

    fun initialize(context: Context) {
        if (isFirebaseInitialized) return

        try {
            // Check if Firebase is already initialized via google-services.json
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                Log.d(TAG, "Firebase already initialized via Provider.")
                setupInstances()
                isFirebaseInitialized = true
            } else {
                // If not, try manual or log warning
                Log.w(TAG, "Firebase not initialized. Ensure google-services.json is present and plugin is applied.")
                // Attempt standard initialization anyway
                FirebaseApp.initializeApp(context)
                setupInstances()
                isFirebaseInitialized = true
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize Firebase: ${e.message}", e)
        }
    }

    private fun setupInstances() {
        authInstance = FirebaseAuth.getInstance()
        firestoreInstance = FirebaseFirestore.getInstance()
        
        authInstance?.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                fetchRealtimeProfile(firebaseUser.uid)
                fetchRealtimeAppointments(firebaseUser.uid)
                fetchRealtimeChats(firebaseUser.uid)
            } else {
                _currentUserFlow.value = null
            }
        }
    }

    private fun fetchRealtimeProfile(uid: String) {
        firestoreInstance?.collection("users")?.document(uid)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java)?.let {
                        _currentUserFlow.value = it
                    }
                }
            }
    }

    private fun fetchRealtimeAppointments(uid: String) {
        firestoreInstance?.collection("appointments")
            ?.whereEqualTo("userId", uid)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.toObjects(Appointment::class.java)
                    _appointmentsFlow.value = list.sortedBy { it.date + " " + it.time }
                }
            }
    }

    private fun fetchRealtimeChats(uid: String) {
        firestoreInstance?.collection("chats")
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _chatsFlow.value = snapshot.toObjects(Chat::class.java)
                }
            }
    }

    fun isUsingRealFirebase(): Boolean = isFirebaseInitialized && authInstance != null

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val auth = authInstance
        if (isUsingRealFirebase() && auth != null) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        val initialProfile = UserProfile(
                            uid = user.uid,
                            email = email,
                            firstName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                            lastName = "User",
                            dob = "1995-10-10",
                            gender = "Male",
                            profilePicUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop"
                        )
                        _currentUserFlow.value = initialProfile
                        saveUserProfile(initialProfile, onSuccess, onFailure)
                    } else {
                        onFailure("Failed creating user")
                    }
                }
                .addOnFailureListener { onFailure(it.localizedMessage ?: "Auth error") }
        } else {
            onFailure("Firebase not initialized.")
        }
    }

    fun saveUserProfile(profile: UserProfile, onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        val db = firestoreInstance
        if (isUsingRealFirebase() && db != null) {
            db.collection("users").document(profile.uid).set(profile)
                .addOnSuccessListener {
                    _currentUserFlow.value = profile
                    onSuccess()
                }
                .addOnFailureListener { onFailure(it.localizedMessage ?: "Firestore error") }
        } else {
            _currentUserFlow.value = profile
            onSuccess()
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val auth = authInstance
        if (isUsingRealFirebase() && auth != null) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it.localizedMessage ?: "Login failed") }
        } else {
            onFailure("Firebase not initialized.")
        }
    }

    fun logout() {
        authInstance?.signOut()
        _currentUserFlow.value = null
    }

    fun addAppointment(appointment: Appointment, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val db = firestoreInstance
        val item = if (appointment.id.isEmpty()) appointment.copy(id = UUID.randomUUID().toString()) else appointment

        if (isUsingRealFirebase() && db != null) {
            db.collection("appointments").document(item.id).set(item)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it.localizedMessage ?: "Firestore error") }
        } else {
            val currentList = _appointmentsFlow.value.toMutableList()
            currentList.add(item)
            _appointmentsFlow.value = currentList
            onSuccess()
        }
    }

    fun updateAppointmentStatus(appointmentId: String, status: String, onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        val db = firestoreInstance
        if (isUsingRealFirebase() && db != null) {
            db.collection("appointments").document(appointmentId).update("status", status)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it.localizedMessage ?: "Firestore error") }
        } else {
            val currentList = _appointmentsFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == appointmentId }
            if (index != -1) {
                currentList[index] = currentList[index].copy(status = status)
                _appointmentsFlow.value = currentList
            }
            onSuccess()
        }
    }

    fun getMessagesFlow(chatId: String): Flow<List<Message>> {
        val db = firestoreInstance
        if (isUsingRealFirebase() && db != null) {
            return callbackFlow {
                val listener = db.collection("chats").document(chatId).collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            trySend(snapshot.toObjects(Message::class.java))
                        }
                    }
                awaitClose { listener.remove() }
            }
        } else {
            if (!messagesMap.containsKey(chatId)) {
                messagesMap[chatId] = MutableStateFlow(emptyList())
            }
            return messagesMap[chatId]!!
        }
    }

    fun sendMessage(chatId: String, text: String, senderId: String = "user") {
        val msg = Message(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        val db = firestoreInstance
        if (isUsingRealFirebase() && db != null) {
            db.collection("chats").document(chatId).collection("messages").document(msg.id).set(msg)
                .addOnSuccessListener {
                    db.collection("chats").document(chatId).update("lastMessage", text, "lastMessageTime", "Just now")
                }
        } else {
            val flow = messagesMap.getOrPut(chatId) { MutableStateFlow(emptyList()) }
            flow.value = flow.value + msg
            
            val chatsList = _chatsFlow.value.toMutableList()
            val index = chatsList.indexOfFirst { it.id == chatId }
            if (index != -1) {
                chatsList[index] = chatsList[index].copy(lastMessage = text, lastMessageTime = "Just now")
                _chatsFlow.value = chatsList
            }
        }
    }
}
