package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.R
import com.example.data.*
import com.example.ui.theme.MedicaBlue
import com.example.ui.theme.MedicaLightBg
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Global App Navigation Routes
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val FILL_PROFILE = "fill_profile"
    const val MAIN = "main" // Hub containing Home, Appointments, Chats, Profile
    const val DOCTOR_DETAILS = "doctor_details"
    const val BOOK_CALENDAR = "book_calendar"
    const val BOOK_PATIENT = "book_patient"
    const val BOOK_PACKAGE = "book_package"
    const val BOOK_PAYMENT = "book_payment"
    const val BOOK_REVIEW = "book_review"
    const val CHAT_SESSION = "chat_session"
}

@Composable
fun MedicaAppNavigation(viewModel: MedicaViewModel) {
    val navController = rememberNavController()
    val currentUserState by viewModel.currentUser.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(navController, currentUserState != null)
        }
        composable(Routes.LOGIN) {
            LoginScreen(navController, viewModel)
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(navController, viewModel)
        }
        composable(Routes.FILL_PROFILE) {
            FillProfileScreen(navController, viewModel)
        }
        composable(Routes.MAIN) {
            MainHubScreen(navController, viewModel)
        }
        composable(Routes.DOCTOR_DETAILS) {
            DoctorDetailsScreen(navController, viewModel)
        }
        composable(Routes.BOOK_CALENDAR) {
            BookingCalendarScreen(navController, viewModel)
        }
        composable(Routes.BOOK_PATIENT) {
            BookingPatientInfoScreen(navController, viewModel)
        }
        composable(Routes.BOOK_PACKAGE) {
            BookingPackageScreen(navController, viewModel)
        }
        composable(Routes.BOOK_PAYMENT) {
            BookingPaymentScreen(navController, viewModel)
        }
        composable(Routes.BOOK_REVIEW) {
            BookingReviewScreen(navController, viewModel)
        }
        composable(Routes.CHAT_SESSION + "/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "chat_doc1"
            ChatSessionScreen(navController, viewModel, chatId)
        }
    }
}

// 1. SPLASH SCREEN (Pulsing heart beats animations)
@Composable
fun SplashScreen(navController: NavController, isLoggedIn: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        delay(2000)
        if (isLoggedIn) {
            navController.navigate(Routes.MAIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        } else {
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .drawBehind {
                        drawCircle(
                            color = MedicaBlue.copy(alpha = 0.15f * pulse),
                            radius = size.minDimension / 1.6f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.medica_logo_1779705445131),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Medica",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MedicaBlue,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Health, One Tap Away.",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// 2. LOGIN SCREEN
@Composable
fun LoginScreen(navController: NavController, viewModel: MedicaViewModel) {
    var email by remember { mutableStateOf("nifrassubairwork@gmail.com") }
    var password by remember { mutableStateOf("password123") }
    var rememberMe by remember { mutableStateOf(true) }

    val authState by viewModel.authUiState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            navController.navigate(Routes.MAIN) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.medica_logo_1779705445131),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Medica",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "Login to Your Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("nifrassubairwork@gmail.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Enter password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = MedicaBlue)
                    )
                    Text(
                        text = "Remember me",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (authState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = MedicaBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (authState is AuthUiState.Error) {
                    Text(
                        text = (authState as AuthUiState.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                ) {
                    Text(
                        text = "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "or",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.login("nifrassubairwork@gmail.com", "password") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "Google",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google", fontSize = 14.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.login("nifrassubairwork@gmail.com", "password") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Apple",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apple", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Sign up",
                        color = MedicaBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(Routes.SIGN_UP)
                        }
                    )
                }
            }
        }
    }
}

// 3. SIGN UP SCREEN
@Composable
fun SignUpScreen(navController: NavController, viewModel: MedicaViewModel) {
    var email by remember { mutableStateOf("nifrassubairwork@gmail.com") }
    var password by remember { mutableStateOf("password123") }
    var confirmPassword by remember { mutableStateOf("password123") }
    var rememberMe by remember { mutableStateOf(true) }

    val authState by viewModel.authUiState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            navController.navigate(Routes.FILL_PROFILE) {
                popUpTo(Routes.SIGN_UP) { inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.medica_logo_1779705445131),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Medica",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Create New Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("nifrassubairwork@gmail.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password") },
                    placeholder = { Text("Enter password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    placeholder = { Text("Re-enter password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = MedicaBlue)
                    )
                    Text(
                        text = "Remember me",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (authState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = MedicaBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (authState is AuthUiState.Error) {
                    Text(
                        text = (authState as AuthUiState.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = { viewModel.signUp(email, password, confirmPassword) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                ) {
                    Text(
                        text = "Create An Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Login",
                        color = MedicaBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.SIGN_UP) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

// 4. FILL USER PROFILE
@Composable
fun FillProfileScreen(navController: NavController, viewModel: MedicaViewModel) {
    var firstName by remember { mutableStateOf("James") }
    var lastName by remember { mutableStateOf("Kelvin") }
    var dob by remember { mutableStateOf("1994-04-12") }
    var gender by remember { mutableStateOf("Male") }

    val authState by viewModel.authUiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fill Your Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop",
                        contentDescription = "Avatar Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MedicaBlue)
                            .clickable { /* Simulate */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Date")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Select")
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (authState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = MedicaBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        viewModel.updateProfile(firstName, lastName, dob, gender)
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.FILL_PROFILE) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                ) {
                    Text(
                        text = "Create An Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 5. MAIN HUB SCREEN
@Composable
fun MainHubScreen(navController: NavController, viewModel: MedicaViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicaBlue,
                        selectedTextColor = MedicaBlue,
                        indicatorColor = MedicaBlue.copy(alpha = 0.12f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.DateRange else Icons.Outlined.DateRange,
                            contentDescription = "Appointments"
                        )
                    },
                    label = { Text("Appointment", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicaBlue,
                        selectedTextColor = MedicaBlue,
                        indicatorColor = MedicaBlue.copy(alpha = 0.12f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Default.MailOutline else Icons.Outlined.MailOutline,
                            contentDescription = "Chats"
                        )
                    },
                    label = { Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicaBlue,
                        selectedTextColor = MedicaBlue,
                        indicatorColor = MedicaBlue.copy(alpha = 0.12f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Default.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicaBlue,
                        selectedTextColor = MedicaBlue,
                        indicatorColor = MedicaBlue.copy(alpha = 0.12f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HomeScreenContent(navController, viewModel)
                1 -> AppointmentsScreenContent(navController, viewModel)
                2 -> ChatListScreenContent(navController, viewModel)
                3 -> ProfileScreenContent(navController, viewModel)
            }
        }
    }
}

// 5a. SCREEN: HOMEPAGE
@Composable
fun HomeScreenContent(navController: NavController, viewModel: MedicaViewModel) {
    val searchVal by viewModel.searchQuery.collectAsStateWithLifecycle()
    val doctorsList by viewModel.doctors.collectAsStateWithLifecycle()
    val appointmentsList by viewModel.appointments.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUser.collectAsStateWithLifecycle()

    val currentUpcoming = appointmentsList.firstOrNull { it.status == "upcoming" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = userProfile?.profilePicUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop",
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Hello ${userProfile?.firstName ?: "James"}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Good Morning",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { /* Demo click */ },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Black
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchVal,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search doctor...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_doctor_input"),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                },
                singleLine = true
            )
        }

        item {
            if (currentUpcoming != null) {
                Text(
                    text = "Upcoming Visit",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val doc = viewModel.doctors.value.firstOrNull { it.id == currentUpcoming.doctorId }
                            if (doc != null) {
                                viewModel.selectedDoctor = doc
                                navController.navigate(Routes.DOCTOR_DETAILS)
                            }
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MedicaBlue)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = currentUpcoming.doctorImage,
                            contentDescription = "Doctor Thumbnail",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUpcoming.doctorName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = currentUpcoming.doctorSpecialty,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Date",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentUpcoming.date}  |  ${currentUpcoming.time}",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                navController.navigate(Routes.CHAT_SESSION + "/chat_doc1")
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Active call/chat",
                                tint = Color.White
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No visits",
                            tint = MedicaBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "No Scheduled Visits",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Tap any doctor below to book a schedule.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Specialties",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val tags = listOf(
                    Triple("Neurologist", Icons.Default.Info, Color(0xFFF0F4FF)),
                    Triple("Cardiologist", Icons.Default.Favorite, Color(0xFFFFECEF)),
                    Triple("Dentist", Icons.Default.Face, Color(0xFFE8F9ED)),
                    Triple("ENT Special", Icons.Default.Call, Color(0xFFFFF9EB))
                )
                items(tags) { tag ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            viewModel.updateSearchQuery(tag.first)
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(tag.third),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = tag.second, contentDescription = tag.first, tint = MedicaBlue)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = tag.first, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                }
            }
        }

        item {
            Text(
                text = "Popular Doctors",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(doctorsList) { doc ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectedDoctor = doc
                        navController.navigate(Routes.DOCTOR_DETAILS)
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = doc.image,
                        contentDescription = doc.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = doc.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = doc.specialty,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = doc.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "(${doc.reviewsCount} reviews)", fontSize = 11.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$${String.format(Locale.US, "%.2f", doc.fee)} / consultation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicaBlue
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.selectedDoctor = doc
                            navController.navigate(Routes.DOCTOR_DETAILS)
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MedicaBlue.copy(alpha = 0.12f))
                    ) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Open", tint = MedicaBlue)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// 5b. SCREEN: APPOINTMENT LISTING
@Composable
fun AppointmentsScreenContent(navController: NavController, viewModel: MedicaViewModel) {
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    var selectedTabState by remember { mutableStateOf(0) } // 0 = Upcoming, 1 = Completed, 2 = Cancel

    val filteredList = when (selectedTabState) {
        0 -> appointments.filter { it.status == "upcoming" }
        1 -> appointments.filter { it.status == "completed" }
        else -> appointments.filter { it.status == "cancelled" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "My Appointments",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTabState,
            containerColor = Color.Transparent,
            contentColor = MedicaBlue
        ) {
            Tab(selected = selectedTabState == 0, onClick = { selectedTabState = 0 }) {
                Text("Upcoming", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Tab(selected = selectedTabState == 1, onClick = { selectedTabState = 1 }) {
                Text("Completed", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Tab(selected = selectedTabState == 2, onClick = { selectedTabState = 2 }) {
                Text("Cancelled", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "None", tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No appointments found here", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredList) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = entry.doctorImage,
                                    contentDescription = entry.doctorName,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.doctorName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(entry.doctorSpecialty, color = Color.Gray, fontSize = 12.sp)
                                }

                                Icon(
                                    imageVector = if (entry.packageName == "Video Call") Icons.Default.PlayArrow else Icons.Default.Call,
                                    contentDescription = "Package type",
                                    tint = MedicaBlue
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

                            Row {
                                Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(entry.date, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(entry.time, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (entry.status == "upcoming") {
                                    OutlinedButton(
                                        onClick = { viewModel.modifyAppointmentStatus(entry.id, "cancelled") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Cancel Visit")
                                    }

                                    Button(
                                        onClick = {
                                            val doc = viewModel.doctors.value.firstOrNull { it.id == entry.doctorId }
                                            if (doc != null) {
                                                viewModel.selectedDoctor = doc
                                                navController.navigate(Routes.BOOK_CALENDAR)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                                    ) {
                                        Text("Reschedule")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val doc = viewModel.doctors.value.firstOrNull { it.id == entry.doctorId }
                                            if (doc != null) {
                                                viewModel.selectedDoctor = doc
                                                navController.navigate(Routes.BOOK_CALENDAR)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                                    ) {
                                        Text("Re-Book Appointment")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5c. SCREEN: CHATS LIST INBOX
@Composable
fun ChatListScreenContent(navController: NavController, viewModel: MedicaViewModel) {
    val chatsList by viewModel.chats.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Messages Inbox",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatsList) { chat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Routes.CHAT_SESSION + "/${chat.id}") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = chat.doctorImage,
                                contentDescription = chat.doctorName,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.Green)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = chat.doctorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = chat.lastMessageTime,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = chat.lastMessage,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5d. SCREEN: PROFILE SETTINGS OPTIONS
@Composable
fun ProfileScreenContent(navController: NavController, viewModel: MedicaViewModel) {
    val profile by viewModel.currentUser.collectAsStateWithLifecycle()
    var showLogoutPrompt by remember { mutableStateOf(false) }

    if (showLogoutPrompt) {
        AlertDialog(
            onDismissRequest = { showLogoutPrompt = false },
            title = { Text("Sure You Want to Leave?") },
            text = { Text("You will need to re-verify your details to access consultations securely again.", fontSize = 14.sp) },
            dismissButton = {
                TextButton(onClick = { showLogoutPrompt = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutPrompt = false
                        viewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Logout", color = Color.White)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        AsyncImage(
            model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop",
            contentDescription = "User avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, MedicaBlue, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profile?.fullName ?: "James Kelvin",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = profile?.email ?: "jameskelvin@gmail.com",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(30.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list = listOf(
                Pair("Edit Profile", Icons.Default.Person),
                Pair("Payment Methods", Icons.Default.PlayArrow),
                Pair("Security & Locks", Icons.Default.Lock),
                Pair("Invite Friends", Icons.Default.Share),
            )

            list.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Demo action */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = item.second, contentDescription = null, tint = MedicaBlue, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(item.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutPrompt = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Logout", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Red)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// 6. SCREEN: DOCTOR DETAILS
@Composable
fun DoctorDetailsScreen(navController: NavController, viewModel: MedicaViewModel) {
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()
    val doc = viewModel.selectedDoctor ?: doctors.firstOrNull() ?: Doctor()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Doctor Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.selectedDoctor = doc
                        navController.navigate(Routes.BOOK_CALENDAR)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                ) {
                    Text("Book Appointment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = doc.image,
                        contentDescription = doc.name,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(doc.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(doc.specialty, color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", doc.fee)} / consult",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicaBlue
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${doc.experienceYears} YRS", fontWeight = FontWeight.Bold, color = MedicaBlue)
                        Text("Experience", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(doc.rating.toString(), fontWeight = FontWeight.Bold, color = MedicaBlue)
                        Text("Rating", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${doc.patientsCount}+", fontWeight = FontWeight.Bold, color = MedicaBlue)
                        Text("Patients", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Text("About", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(doc.about, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)

            Text("Working Time", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Monday - Friday", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(doc.workingHours, color = MedicaBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// 7. BOOKING STEPS: SELECT CALENDAR
@Composable
fun BookingCalendarScreen(navController: NavController, viewModel: MedicaViewModel) {
    var selectedDateIdx by remember { mutableStateOf(2) } // default Tue 17
    var selectedTimeIdx by remember { mutableStateOf(1) } // default 8:30 PM

    val dates = listOf(
        Triple("15", "Sun", "15 June 2026"),
        Triple("16", "Mon", "16 June 2026"),
        Triple("17", "Tue", "17 June 2026"),
        Triple("18", "Wed", "18 June 2026"),
        Triple("19", "Thu", "19 June 2026"),
        Triple("20", "Fri", "20 June 2026"),
    )

    val times = listOf(
        "8:00 PM",
        "8:30 PM",
        "9:00 PM",
        "9:30 PM"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Text("Booking Appointment", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.bookingDate = dates[selectedDateIdx].third
                        viewModel.bookingTime = times[selectedTimeIdx]
                        navController.navigate(Routes.BOOK_PATIENT)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                ) {
                    Text("Make Appointment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select Date", fontSize = 16.sp, fontWeight = FontWeight.Bold)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(dates.size) { idx ->
                    val d = dates[idx]
                    val isSelected = selectedDateIdx == idx
                    Card(
                        modifier = Modifier
                            .size(width = 64.dp, height = 80.dp)
                            .clickable { selectedDateIdx = idx },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MedicaBlue else Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(d.first, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (isSelected) Color.White else Color.Black)
                            Text(d.second, fontSize = 12.sp, color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Select Time", fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 0..1) {
                        val isSel = selectedTimeIdx == i
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable { selectedTimeIdx = i },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) MedicaBlue else Color.White
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(times[i], fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.Black)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 2..3) {
                        val isSel = selectedTimeIdx == i
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable { selectedTimeIdx = i },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) MedicaBlue else Color.White
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(times[i], fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 8. BOOKING STEPS: PATIENT DETAILS
@Composable
fun BookingPatientInfoScreen(navController: NavController, viewModel: MedicaViewModel) {
    var forMyself by remember { mutableStateOf(true) }
    var patientName by remember { mutableStateOf("James Kelvin") }
    var patientGender by remember { mutableStateOf("Male") }
    var email by remember { mutableStateOf("jameskelvin@gmail.com") }
    var notes by remember { mutableStateOf("Have chest issue and blood pressure variations.") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Text("Patient Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.bookingPatientOption = if (forMyself) "Myself" else "Others"
                        viewModel.bookingPatientName = patientName
                        viewModel.bookingPatientGender = patientGender
                        viewModel.bookingPatientEmail = email
                        viewModel.bookingNotes = notes
                        navController.navigate(Routes.BOOK_PACKAGE)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                ) {
                    Text("Make Appointment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { forMyself = true; patientName = "James Kelvin"; email = "jameskelvin@gmail.com"; patientGender = "Male" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (forMyself) MedicaBlue else Color.LightGray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("For Myself", color = if (forMyself) Color.White else Color.Black)
                }
                Button(
                    onClick = { forMyself = false; patientName = ""; email = ""; patientGender = "" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (!forMyself) MedicaBlue else Color.LightGray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Others", color = if (!forMyself) Color.White else Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = patientName,
                onValueChange = { patientName = it },
                label = { Text("Patient Name") },
                placeholder = { Text("Enter patient's name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = patientGender,
                onValueChange = { patientGender = it },
                label = { Text("Gender") },
                placeholder = { Text("Male / Female") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("patient@gmail.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Disease symptoms / Notes") },
                placeholder = { Text("Brief symptoms details...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )
        }
    }
}

// 9. BOOKING STEPS: SELECT PACKAGE
@Composable
fun BookingPackageScreen(navController: NavController, viewModel: MedicaViewModel) {
    var selectedPkgState by remember { mutableStateOf("Video Call") }

    val packages = listOf(
        Triple("Message", "Chat consultation with medical records uploads.", 25.0),
        Triple("Voice Call", "Direct online high fidelity cellular call consult.", 29.0),
        Triple("Video Call", "Real-time immersive diagnosis online consult.", 35.0),
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Text("Select Package", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = {
                        val selected = packages.first { it.first == selectedPkgState }
                        viewModel.bookingPackageName = selected.first
                        viewModel.bookingPackageFee = selected.third
                        navController.navigate(Routes.BOOK_PAYMENT)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                ) {
                    Text("Make Appointment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            packages.forEach { pkg ->
                val isSelected = selectedPkgState == pkg.first
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.5.dp,
                            if (isSelected) MedicaBlue else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedPkgState = pkg.first },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (pkg.first) {
                                "Message" -> Icons.Default.MailOutline
                                "Voice Call" -> Icons.Default.Call
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = null,
                            tint = MedicaBlue,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MedicaBlue.copy(alpha = 0.12f))
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(pkg.first, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(pkg.second, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Text(
                            text = "$${String.format(Locale.US, "%.0f", pkg.third)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MedicaBlue
                        )
                    }
                }
            }
        }
    }
}

// 10. BOOKING STEPS: PAYMENT METHODS SELECTOR
@Composable
fun BookingPaymentScreen(navController: NavController, viewModel: MedicaViewModel) {
    var selectedCard by remember { mutableStateOf("Visa Card") }

    val cards = listOf(
        Pair("Paypal", Icons.Default.Lock),
        Pair("Master Card", Icons.Default.Favorite),
        Pair("Visa Card", Icons.Default.Star),
        Pair("Apple Pay", Icons.Default.ArrowBack),
        Pair("Google Pay", Icons.Default.Home)
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Text("Select Payment Method", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.bookingPaymentMethod = selectedCard
                        navController.navigate(Routes.BOOK_REVIEW)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                ) {
                    Text("Make Appointment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            cards.forEach { card ->
                val isSel = selectedCard == card.first
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCard = card.first },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = card.second, contentDescription = null, tint = MedicaBlue)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(card.first, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = isSel,
                            onClick = { selectedCard = card.first },
                            colors = RadioButtonDefaults.colors(selectedColor = MedicaBlue)
                        )
                    }
                }
            }
        }
    }
}

// 11. BOOKING STEPS: REVIEW & SUBMIT
@Composable
fun BookingReviewScreen(navController: NavController, viewModel: MedicaViewModel) {
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()
    val d = viewModel.selectedDoctor ?: doctors.firstOrNull() ?: Doctor()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Text("Review Booking", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.makeAppointment {
                            navController.navigate(Routes.MAIN) {
                                popUpTo(Routes.DOCTOR_DETAILS) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicaBlue)
                ) {
                    Text("Pay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = d.image,
                        contentDescription = d.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(d.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(d.specialty, color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReceiptRow("Date", viewModel.bookingDate)
                    ReceiptRow("Time", viewModel.bookingTime)
                    ReceiptRow("Package", viewModel.bookingPackageName)
                    ReceiptRow("Patient", viewModel.bookingPatientName)
                    ReceiptRow("Gender", viewModel.bookingPatientGender)
                    ReceiptRow("Payment Method", viewModel.bookingPaymentMethod)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReceiptRow("Fee", "$${String.format(Locale.US, "%.2f", viewModel.bookingPackageFee)}")
                    ReceiptRow("Tax", "$2.00")
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Text(
                            "$${String.format(Locale.US, "%.2f", viewModel.bookingPackageFee + 2.0)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MedicaBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// 12. SCREEN: LIVE MESSAGING CHAT WINDOW
@Composable
fun ChatSessionScreen(navController: NavController, viewModel: MedicaViewModel, chatId: String) {
    val messagesState by viewModel.getMessagesFlow(chatId).collectAsStateWithLifecycle(initialValue = emptyList())
    var textMessage by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MedicaLightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.width(8.dp))

                AsyncImage(
                    model = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?q=80&w=200&auto=format&fit=crop",
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text("Dr. Daniel Michael", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Active consultation slot", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textMessage,
                    onValueChange = { textMessage = it },
                    placeholder = { Text("Type message.......") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textMessage.trim().isNotEmpty()) {
                            viewModel.sendMessage(chatId, textMessage)
                            textMessage = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MedicaBlue)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send Message", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F0FF))
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Consultation Start",
                        fontWeight = FontWeight.Bold,
                        color = MedicaBlue,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "You can consult your problem to the doctor directly.",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messagesState) { msg ->
                    val isUser = msg.senderId == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 0.dp,
                                bottomEnd = if (isUser) 0.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) MedicaBlue else Color.White
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    color = if (isUser) Color.White else Color.Black,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp)),
                                    color = if (isUser) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                    fontSize = 9.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
    }
}
