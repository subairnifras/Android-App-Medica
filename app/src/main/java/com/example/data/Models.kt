package com.example.data

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dob: String = "",
    val gender: String = "",
    val profilePicUrl: String = ""
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

data class Doctor(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val image: String = "",
    val rating: Double = 4.8,
    val reviewsCount: Int = 124,
    val experienceYears: Int = 10,
    val patientsCount: Int = 5000,
    val fee: Double = 25.0,
    val about: String = "",
    val workingHours: String = "8:00 PM - 10:00 PM"
)

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorSpecialty: String = "",
    val doctorImage: String = "",
    val date: String = "",
    val time: String = "",
    val patientOption: String = "Myself", // Myself or Others
    val patientName: String = "",
    val patientGender: String = "",
    val patientEmail: String = "",
    val notes: String = "",
    val packageName: String = "Message", // Message, Voice Call, Video Call
    val packageFee: Double = 25.0,
    val tax: Double = 2.0,
    val total: Double = 27.0,
    val paymentMethod: String = "Visa Card",
    val status: String = "upcoming" // upcoming, completed, cancelled
)

data class Chat(
    val id: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorImage: String = "",
    val lastMessage: String = "",
    val lastMessageTime: String = ""
)

data class Message(
    val id: String = "",
    val senderId: String = "", // "user" or "doctor"
    val text: String = "",
    val timestamp: Long = 0L
)
