package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
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

    private var authInstance: FirebaseAuth? = null
    private var firestoreInstance: FirebaseFirestore? = null

    private val _currentUserFlow = MutableStateFlow<UserProfile?>(null)
    val currentUserFlow: StateFlow<UserProfile?> = _currentUserFlow.asStateFlow()

    private val _appointmentsFlow = MutableStateFlow<List<Appointment>>(emptyList())
    val appointmentsFlow: StateFlow<List<Appointment>> = _appointmentsFlow.asStateFlow()

    private val _chatsFlow = MutableStateFlow<List<Chat>>(emptyList())
    val chatsFlow: StateFlow<List<Chat>> = _chatsFlow.asStateFlow()

    private val _doctorsFlow = MutableStateFlow<List<Doctor>>(emptyList())
    val doctorsListFlow: StateFlow<List<Doctor>> = _doctorsFlow.asStateFlow()

    // Backup hardcoded list for seeding Firestore
    val initialDoctors = listOf(
        Doctor(id = "doc1", name = "Dr. Daniel Michael", specialty = "Neurologist", image = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?q=80&w=200&auto=format&fit=crop", rating = 4.8, reviewsCount = 4821, experienceYears = 12, patientsCount = 6200, fee = 25.0, about = "Dr. Daniel Michael is a highly experienced Neurologist.", workingHours = "8:00 PM - 10:00 PM"),
        Doctor(id = "doc2", name = "Dr. Mohamed Zahran", specialty = "Cardiologist", image = "https://images.unsplash.com/photo-1537368910025-700350fe46c7?q=80&w=200&auto=format&fit=crop", rating = 4.9, reviewsCount = 3120, experienceYears = 15, patientsCount = 8400, fee = 29.0, about = "Dr. Mohamed Zahran offers world-class cardiovascular diagnostics.", workingHours = "7:00 PM - 9:30 PM"),
        Doctor(id = "doc3", name = "Dr. Fazira Shafi", specialty = "Dentist", image = "https://images.unsplash.com/photo-1594824813573-246434de83fb?q=80&w=200&auto=format&fit=crop", rating = 4.7, reviewsCount = 1890, experienceYears = 8, patientsCount = 3800, fee = 19.0, about = "Dr. Fazira Shafi is passionate about oral health.", workingHours = "9:00 AM - 1:00 PM"),
        Doctor(id = "doc4", name = "Dr. Arjuna Raman", specialty = "Dentist", image = "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=200&auto=format&fit=crop", rating = 4.6, reviewsCount = 980, experienceYears = 6, patientsCount = 2200, fee = 22.0, about = "Dr. Arjuna Raman is skilled in orthodontist procedures.", workingHours = "4:00 PM - 7:00 PM")
    )

    fun initialize(context: Context) {
        if (isFirebaseInitialized) return
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                setupInstances()
                isFirebaseInitialized = true
            } else {
                FirebaseApp.initializeApp(context)
                setupInstances()
                isFirebaseInitialized = true
            }
            seedDoctorsIfEmpty()
        } catch (e: Throwable) {
            Log.e(TAG, "Firebase Init Error: ${e.message}")
        }
    }

    private fun setupInstances() {
        authInstance = FirebaseAuth.getInstance()
        firestoreInstance = FirebaseFirestore.getInstance()
        
        authInstance?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                fetchRealtimeProfile(user.uid)
                fetchRealtimeAppointments(user.uid)
                fetchRealtimeChats(user.uid)
            } else {
                _currentUserFlow.value = null
                _appointmentsFlow.value = emptyList()
                _chatsFlow.value = emptyList()
            }
        }
        fetchRealtimeDoctors()
    }

    private fun seedDoctorsIfEmpty() {
        val db = firestoreInstance ?: return
        db.collection("doctors").get().addOnSuccessListener { docs ->
            if (docs.isEmpty) {
                initialDoctors.forEach { doc ->
                    db.collection("doctors").document(doc.id).set(doc)
                }
                Log.d(TAG, "Seeded initial doctors into Firestore.")
            }
        }
    }

    private fun fetchRealtimeDoctors() {
        firestoreInstance?.collection("doctors")?.addSnapshotListener { snapshot, _ ->
            snapshot?.let { _doctorsFlow.value = it.toObjects(Doctor::class.java) }
        }
    }

    private fun fetchRealtimeProfile(uid: String) {
        firestoreInstance?.collection("users")?.document(uid)?.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                _currentUserFlow.value = snapshot.toObject(UserProfile::class.java)
            }
        }
    }

    private fun fetchRealtimeAppointments(uid: String) {
        firestoreInstance?.collection("appointments")
            ?.whereEqualTo("userId", uid)
            ?.addSnapshotListener { snapshot, _ ->
                snapshot?.let { _appointmentsFlow.value = it.toObjects(Appointment::class.java).sortedBy { a -> a.date } }
            }
    }

    private fun fetchRealtimeChats(uid: String) {
        // In a real app, you'd filter chats where participants contains uid
        firestoreInstance?.collection("chats")?.addSnapshotListener { snapshot, _ ->
            snapshot?.let { _chatsFlow.value = it.toObjects(Chat::class.java) }
        }
    }

    fun isUsingRealFirebase(): Boolean = isFirebaseInitialized && authInstance != null

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        authInstance?.createUserWithEmailAndPassword(email, password)?.addOnSuccessListener { result ->
            val user = result.user
            if (user != null) {
                val profile = UserProfile(uid = user.uid, email = email, firstName = "New", lastName = "User")
                saveUserProfile(profile, onSuccess, onFailure)
            }
        }?.addOnFailureListener { onFailure(it.localizedMessage ?: "Sign up failed") }
    }

    fun saveUserProfile(profile: UserProfile, onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        firestoreInstance?.collection("users")?.document(profile.uid)?.set(profile)
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { onFailure(it.localizedMessage ?: "Firestore error") }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        authInstance?.signInWithEmailAndPassword(email, password)
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { onFailure(it.localizedMessage ?: "Login failed") }
    }

    fun logout() { authInstance?.signOut() }

    fun addAppointment(appointment: Appointment, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val db = firestoreInstance ?: return
        val id = if (appointment.id.isEmpty()) UUID.randomUUID().toString() else appointment.id
        db.collection("appointments").document(id).set(appointment.copy(id = id))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.localizedMessage ?: "Booking error") }
    }

    fun updateAppointmentStatus(id: String, status: String) {
        firestoreInstance?.collection("appointments")?.document(id)?.update("status", status)
    }

    fun getMessagesFlow(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestoreInstance?.collection("chats")?.document(chatId)?.collection("messages")
            ?.orderBy("timestamp", Query.Direction.ASCENDING)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) close(error) else snapshot?.let { trySend(it.toObjects(Message::class.java)) }
            }
        awaitClose { listener?.remove() }
    }

    fun sendMessage(chatId: String, text: String, senderId: String = "user") {
        val db = firestoreInstance ?: return
        val msgId = UUID.randomUUID().toString()
        val msg = Message(id = msgId, senderId = senderId, text = text, timestamp = System.currentTimeMillis())
        db.collection("chats").document(chatId).collection("messages").document(msgId).set(msg)
            .addOnSuccessListener {
                db.collection("chats").document(chatId).update("lastMessage", text, "lastMessageTime", "Just now")
            }
    }
}
