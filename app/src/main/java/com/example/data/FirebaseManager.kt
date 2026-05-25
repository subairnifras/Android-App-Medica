package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.BuildConfig
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

    // Fallback/Simulated Data Engine (to guarantee 100% functionality and zero crashes)
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
            // Retrieve keys from BuildConfig if existing
            val apiKey = getBuildConfigValue("FIREBASE_API_KEY")
            val appId = getBuildConfigValue("FIREBASE_APP_ID")
            val projectId = getBuildConfigValue("FIREBASE_PROJECT_ID")

            if (!apiKey.isNullOrEmpty() && apiKey != "YOUR_FIREBASE_API_KEY" && !appId.isNullOrEmpty() && !projectId.isNullOrEmpty()) {
                Log.d(TAG, "Configuring Firebase programmatically with provided options.")
                
                // If FirebaseApp is already initialized under the default name (e.g., from a auto-generated google-services config resource),
                // use that, otherwise initialize it. This avoids IllegalStateException of duplicate registrations.
                val apps = com.google.firebase.FirebaseApp.getApps(context)
                if (apps.isEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setApplicationId(appId)
                        .setProjectId(projectId)
                        .build()
                    FirebaseApp.initializeApp(context, options)
                }
                
                isFirebaseInitialized = true
                authInstance = FirebaseAuth.getInstance()
                firestoreInstance = FirebaseFirestore.getInstance()
                Log.d(TAG, "Firebase successfully initialized programmatically!")

                // Listen to Real-time changes from Firestore if authenticated
                authInstance?.addAuthStateListener { firebaseAuth ->
                    try {
                        val firebaseUser = firebaseAuth.currentUser
                        if (firebaseUser != null) {
                            fetchRealtimeProfile(firebaseUser.uid)
                            fetchRealtimeAppointments(firebaseUser.uid)
                            fetchRealtimeChats(firebaseUser.uid)
                        } else {
                            _currentUserFlow.value = null
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG, "Auth state listener callback exception caught: ${t.message}", t)
                    }
                }
            } else {
                Log.w(TAG, "Firebase credentials not set or incomplete. Running in Simulation/Local Mode.")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed programmatically initialize Firebase, switching to Local Sandbox mode: ${e.message}", e)
        }
    }

    private fun getBuildConfigValue(fieldName: String): String? {
        return try {
            val clazz = Class.forName("${contextPackageName}.BuildConfig")
            val field = clazz.getField(fieldName)
            field.get(null) as? String
        } catch (e: Throwable) {
            null
        }
    }

    private val contextPackageName: String
        get() = "com.example" // fallback, matches our namespace

    // Real-time listener for User Profile in firestore
    private fun fetchRealtimeProfile(uid: String) {
        try {
            val db = firestoreInstance ?: return
            db.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen to profile failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val profile = snapshot.toObject(UserProfile::class.java)
                            _currentUserFlow.value = profile
                        } catch (t: Throwable) {
                            Log.e(TAG, "Parsing profile snap failed: ${t.message}", t)
                        }
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "fetchRealtimeProfile subscription failed: ${t.message}", t)
        }
    }

    // Real-time listener for Appointments in firestore
    private fun fetchRealtimeAppointments(uid: String) {
        try {
            val db = firestoreInstance ?: return
            db.collection("appointments")
                .whereEqualTo("userId", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen to appointments failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        try {
                            val list = snapshot.toObjects(Appointment::class.java)
                            _appointmentsFlow.value = list.sortedBy { it.date + " " + it.time }
                        } catch (t: Throwable) {
                            Log.e(TAG, "Parsing appointments snap failed: ${t.message}", t)
                        }
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "fetchRealtimeAppointments subscription failed: ${t.message}", t)
        }
    }

    // Real-time listener for Chats in firestore
    private fun fetchRealtimeChats(uid: String) {
        try {
            val db = firestoreInstance ?: return
            db.collection("chats")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen to chats failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        try {
                            val list = snapshot.toObjects(Chat::class.java)
                            _chatsFlow.value = list
                        } catch (t: Throwable) {
                            Log.e(TAG, "Parsing chats snap failed: ${t.message}", t)
                        }
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "fetchRealtimeChats subscription failed: ${t.message}", t)
        }
    }

    fun isUsingRealFirebase(): Boolean {
        return isFirebaseInitialized && authInstance != null
    }

    // AUTH APIs
    fun signUp(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
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
                            lastName = "Kelvin",
                            dob = "1995-10-10",
                            gender = "Male",
                            profilePicUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop"
                        )
                        _currentUserFlow.value = initialProfile
                        saveUserProfile(initialProfile, { onSuccess() }, { onFailure(it) })
                    } else {
                        onFailure("Failed creating user in Firebase")
                    }
                }
                .addOnFailureListener {
                    onFailure(it.localizedMessage ?: "Firebase Authentication error")
                }
        } else {
            // Local simulation Mode
            if (email.contains("@") && password.length >= 6) {
                val simulatedUid = UUID.randomUUID().toString()
                val profile = UserProfile(
                    uid = simulatedUid,
                    email = email,
                    firstName = "James",
                    lastName = "Kelvin",
                    dob = "1994-04-12",
                    gender = "Male",
                    profilePicUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop"
                )
                _currentUserFlow.value = profile
                onSuccess()
            } else {
                onFailure("Please enter a valid email and matching passwords (min 6 characters)")
            }
        }
    }

    fun saveUserProfile(
        profile: UserProfile,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val db = firestoreInstance
        if (isUsingRealFirebase() && db != null) {
            db.collection("users").document(profile.uid)
                .set(profile)
                .addOnSuccessListener {
                    _currentUserFlow.value = profile
                    onSuccess()
                }
                .addOnFailureListener {
                    onFailure(it.localizedMessage ?: "Failed writing profile to Firestore")
                }
        } else {
            _currentUserFlow.value = profile
            onSuccess()
        }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val auth = authInstance
        if (isUsingRealFirebase() && auth != null) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener {
                    onFailure(it.localizedMessage ?: "Authentication details incorrect")
                }
        } else {
            // Simulated login: accept any user
            if (email.isNotEmpty() && password.isNotEmpty()) {
                val userMail = email.ifEmpty { "jameskelvin@gmail.com" }
                val profile = UserProfile(
                    uid = "simulated_user_123",
                    email = userMail,
                    firstName = "James",
                    lastName = "Kelvin",
                    dob = "1994-04-12",
                    gender = "Male",
                    profilePicUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop"
                )
                _currentUserFlow.value = profile
                onSuccess()
            } else {
                onFailure("Email and Password fields are required!")
            }
        }
    }

    fun logout() {
        val auth = authInstance
        if (isUsingRealFirebase() && auth != null) {
            auth.signOut()
        }
        _currentUserFlow.value = null
        // maintain list in simulation or clear if logged out
    }

    // APPOINTMENTS
    fun addAppointment(
        appointment: Appointment,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val db = firestoreInstance
        val item = if (appointment.id.isEmpty()) appointment.copy(id = UUID.randomUUID().toString()) else appointment

        if (isUsingRealFirebase() && db != null) {
            db.collection("appointments").document(item.id)
                .set(item)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it.localizedMessage ?: "Firestore write error") }
        } else {
            // Simulated add
            val currentList = _appointmentsFlow.value.toMutableList()
            currentList.removeAll { it.id == item.id }
            currentList.add(item)
            _appointmentsFlow.value = currentList
            onSuccess()
        }
    }

    fun updateAppointmentStatus(
        appointmentId: String,
        status: String,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val db = firestoreInstance
        if (isUsingRealFirebase() && db != null) {
            db.collection("appointments").document(appointmentId)
                .update("status", status)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it.localizedMessage ?: "Firestore update error") }
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

    // REAL-TIME MESSAGING CHATS FLOW
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
                            val messages = snapshot.toObjects(Message::class.java)
                            trySend(messages)
                        }
                    }
                awaitClose { listener.remove() }
            }
        } else {
            // Simulated local messages flow
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
            // Write to Firestore db
            db.collection("chats").document(chatId).collection("messages").document(msg.id)
                .set(msg)
                .addOnSuccessListener {
                    // Update last message in chat document
                    db.collection("chats").document(chatId)
                        .update("lastMessage", text, "lastMessageTime", "Just now")
                }
        } else {
            // Update local map flow
            if (!messagesMap.containsKey(chatId)) {
                messagesMap[chatId] = MutableStateFlow(emptyList())
            }
            val flow = messagesMap[chatId]!!
            val currentMessages = flow.value.toMutableList()
            currentMessages.add(msg)
            flow.value = currentMessages

            // Update in chats list
            val chatsList = _chatsFlow.value.toMutableList()
            val index = chatsList.indexOfFirst { it.id == chatId }
            if (index != -1) {
                chatsList[index] = chatsList[index].copy(
                    lastMessage = text,
                    lastMessageTime = "Just now"
                )
                _chatsFlow.value = chatsList
            }

            // Simple doctor auto-reply after a short delay
            if (senderId == "user") {
                val docResponse = when {
                    text.contains("hello", ignoreCase = true) || text.contains("hi", ignoreCase = true) -> "Hello! I hope you are doing well today. How can I assist you with your health?"
                    text.contains("headache", ignoreCase = true) -> "I recommend resting in a calm dark room and keeping hydrated. If it persists, let's look at a pain reliever dosage."
                    text.contains("thank", ignoreCase = true) -> "You are welcome. Take care and let me know if you need any follow-up!"
                    else -> "Thank you for the message. I have recorded your symptom and we will discuss it in detail during our scheduled consultation."
                }
                // Simulate delay
                Thread {
                    try {
                        Thread.sleep(1200)
                        val replyMsg = Message(
                            id = UUID.randomUUID().toString(),
                            senderId = "doctor",
                            text = docResponse,
                            timestamp = System.currentTimeMillis()
                        )
                        val updated = flow.value.toMutableList()
                        updated.add(replyMsg)
                        flow.value = updated

                        val freshChats = _chatsFlow.value.toMutableList()
                        val chatIdx = freshChats.indexOfFirst { it.id == chatId }
                        if (chatIdx != -1) {
                            freshChats[chatIdx] = freshChats[chatIdx].copy(
                                lastMessage = docResponse,
                                lastMessageTime = "1min ago"
                            )
                            _chatsFlow.value = freshChats
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }
    }
}
