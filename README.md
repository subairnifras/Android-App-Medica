# Medica - Healthcare Management App

Medica is a modern, full-stack Android application designed to provide a seamless experience for patients to connect with healthcare providers. Built with Jetpack Compose and powered by Firebase, it offers a robust platform for doctor discovery, appointment scheduling, and real-time communication.

##  Key Features

*   **Secure Authentication:** Integrated with Firebase Authentication for safe and easy sign-in using email and password.
*   **User Profiles:** personalized onboarding flow to capture patient details, persisted in Cloud Firestore.
*   **Doctor Discovery:** 
    *   Search and filter doctors by name or specialty.
    *   Detailed doctor profiles including ratings, experience, and patient reviews.
*   **Advanced Booking System:** 
    *   Multi-step wizard for choosing dates, time slots, and patient info.
    *   Flexible consultation packages: Message, Voice Call, and Video Call.
*   **Appointment Management:** 
    *   Real-time tracking of Upcoming, Completed, and Cancelled appointments.
    *   In-app cancellation and rescheduling capabilities.
*   **Real-time Messaging:** Direct chat communication with doctors powered by Firestore real-time listeners.
*   **Notifications:** Visual cues for new messages and upcoming visits.

##  Tech Stack

*   **Language:** Kotlin (2.0.21)
*   **UI Framework:** Jetpack Compose (Modern Declarative UI)
*   **Backend:** 
    *   **Firebase Authentication:** User identity management.
    *   **Cloud Firestore:** Real-time NoSQL database for profiles, appointments, and chats.
*   **Architecture:** MVVM (Model-View-ViewModel) for clean separation of concerns.
*   **Networking & Image Loading:** 
    *   **Coil:** Asynchronous image loading for profile and doctor pictures.
*   **Concurrency:** Kotlin Coroutines & StateFlow for reactive data handling.

##  Project Structure

*   `app/src/main/java/com/example/data/`: Data models and `FirebaseManager` (central API engine).
*   `app/src/main/java/com/example/ui/`: UI components, including `MedicaViewModel` and Compose screens.
*   `app/src/main/java/com/example/ui/theme/`: Custom branding, color schemes, and typography.
*   `app/src/google-services.json`: Firebase configuration file.

##  Getting Started

### Prerequisites
*   Android Studio Ladybug or newer.
*   JDK 11+.
*   A Firebase project with **Authentication** (Email/Password) and **Firestore** enabled.

### Installation
1.  Clone the repository.
2.  Add your `google-services.json` to the `app/` directory.
3.  Sync Gradle and run the app on an emulator or physical device.

##  Recent Updates
*   Migrated from simulated local state to live Firebase Cloud Firestore integration.
*   Standardized dependency versions for Kotlin 2.0.21 compatibility.
*   Integrated custom branding and adaptive launcher icons.

---
*Created as part of the Medica UX development initiative.*
