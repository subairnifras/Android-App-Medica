package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val profile: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class MedicaViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    // Auth States
    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    val currentUser: StateFlow<UserProfile?> = FirebaseManager.currentUserFlow
    val appointments: StateFlow<List<Appointment>> = FirebaseManager.appointmentsFlow
    val chats: StateFlow<List<Chat>> = FirebaseManager.chatsFlow
    val doctors: StateFlow<List<Doctor>> = FirebaseManager.doctorsListFlow

    // Booking Wizard temporary state
    var selectedDoctor: Doctor? = null
    var bookingDate: String = "17 June 2026"
    var bookingTime: String = "8:30 PM"
    var bookingPatientOption: String = "Myself"
    var bookingPatientName: String = "James Kelvin"
    var bookingPatientGender: String = "Male"
    var bookingPatientEmail: String = "jameskelvin@gmail.com"
    var bookingNotes: String = ""
    var bookingPackageName: String = "Video Call"
    var bookingPackageFee: Double = 25.0
    var bookingPaymentMethod: String = "Visa Card"

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter doctors by search query
    val filteredDoctors: StateFlow<List<Doctor>> = searchQuery
        .combine(doctors) { query, docs ->
            if (query.isEmpty()) docs
            else docs.filter { it.name.contains(query, ignoreCase = true) || it.specialty.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize Firebase on start
        FirebaseManager.initialize(context)

        // Sync with existing logged-in user
        viewModelScope.launch {
            currentUser.collect { profile ->
                if (profile != null) {
                    _authUiState.value = AuthUiState.Success(profile)
                } else {
                    _authUiState.value = AuthUiState.Idle
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // SIGN UP FLOW
    fun signUp(email: String, password: String, confirmPassword: String) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _authUiState.value = AuthUiState.Error("All fields are required!")
            return
        }
        if (password != confirmPassword) {
            _authUiState.value = AuthUiState.Error("Passwords do not match!")
            return
        }
        if (password.length < 6) {
            _authUiState.value = AuthUiState.Error("Password must be at least 6 characters!")
            return
        }

        _authUiState.value = AuthUiState.Loading
        FirebaseManager.signUp(
            email = email,
            password = password,
            onSuccess = {
                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                _authUiState.value = AuthUiState.Error(error)
                Toast.makeText(context, "Sign Up Failed: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    // PROFILE FILL/UPDATE FLOW
    fun updateProfile(firstName: String, lastName: String, dob: String, gender: String) {
        val user = currentUser.value
        if (user == null) {
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        _authUiState.value = AuthUiState.Loading
        val updatedProfile = user.copy(
            firstName = firstName,
            lastName = lastName,
            dob = dob,
            gender = gender
        )

        FirebaseManager.saveUserProfile(updatedProfile, {
            _authUiState.value = AuthUiState.Success(updatedProfile)
            Toast.makeText(context, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show()
        }, { error ->
            _authUiState.value = AuthUiState.Error(error)
            Toast.makeText(context, "Failed to save profile: $error", Toast.LENGTH_LONG).show()
        })
    }

    // LOGIN FLOW
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authUiState.value = AuthUiState.Error("Email and Password are required!")
            return
        }

        _authUiState.value = AuthUiState.Loading
        FirebaseManager.login(
            email = email,
            password = password,
            onSuccess = {
                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                _authUiState.value = AuthUiState.Error(error)
                Toast.makeText(context, "Sign In Failed: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    // LOGOUT
    fun logout() {
        FirebaseManager.logout()
        _authUiState.value = AuthUiState.Idle
    }

    // APPOINTMENTS MANAGEMENT
    fun makeAppointment(onSuccess: () -> Unit) {
        val doc = selectedDoctor ?: return
        val currentUserId = currentUser.value?.uid ?: return

        val appointment = Appointment(
            id = "", 
            userId = currentUserId,
            doctorId = doc.id,
            doctorName = doc.name,
            doctorSpecialty = doc.specialty,
            doctorImage = doc.image,
            date = bookingDate,
            time = bookingTime,
            patientOption = bookingPatientOption,
            patientName = bookingPatientName,
            patientGender = bookingPatientGender,
            patientEmail = bookingPatientEmail,
            notes = bookingNotes,
            packageName = bookingPackageName,
            packageFee = bookingPackageFee,
            tax = 2.0,
            total = bookingPackageFee + 2.0,
            paymentMethod = bookingPaymentMethod,
            status = "upcoming"
        )

        FirebaseManager.addAppointment(
            appointment,
            onSuccess = {
                Toast.makeText(context, "Appointment Booked Successfully!", Toast.LENGTH_SHORT).show()
                onSuccess()
            },
            onFailure = { error ->
                Toast.makeText(context, "Booking Failed: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    fun modifyAppointmentStatus(appointmentId: String, status: String) {
        FirebaseManager.updateAppointmentStatus(appointmentId, status)
        Toast.makeText(context, "Appointment marked as $status", Toast.LENGTH_SHORT).show()
    }

    // MESSAGING API
    fun getMessagesFlow(chatId: String): Flow<List<Message>> {
        return FirebaseManager.getMessagesFlow(chatId)
    }

    fun sendMessage(chatId: String, text: String) {
        if (text.trim().isEmpty()) return
        FirebaseManager.sendMessage(chatId, text, "user")
    }
}
