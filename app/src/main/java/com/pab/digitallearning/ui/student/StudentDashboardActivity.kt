package com.pab.digitallearning.ui.student

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pab.digitallearning.core.MainActivity
import com.pab.digitallearning.core.SessionManager
import com.pab.digitallearning.ui.student.content.StudentContentDetailScreen
import com.pab.digitallearning.ui.student.content.StudentContentViewModel
import com.pab.digitallearning.ui.student.content.StudentQuizPlayScreen
import com.pab.digitallearning.ui.student.home.StudentHomeScreen
import com.pab.digitallearning.ui.student.home.StudentHomeViewModel
import com.pab.digitallearning.ui.student.notification.StudentNotificationScreen
import com.pab.digitallearning.ui.student.notification.StudentNotificationViewModel
import com.pab.digitallearning.ui.student.profile.StudentProfileScreen
import com.pab.digitallearning.ui.student.profile.StudentProfileViewModel
import com.pab.digitallearning.ui.student.subject.StudentSubjectDetailScreen
import com.pab.digitallearning.ui.student.subject.StudentSubjectListScreen
import com.pab.digitallearning.ui.student.subject.StudentSubjectViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

sealed interface StudentScreen {
    object Home : StudentScreen
    object Subjects : StudentScreen
    object Notifications : StudentScreen
    object Profile : StudentScreen
    data class SubjectDetail(val id: Long, val name: String) : StudentScreen
    data class ContentDetail(val id: Long, val title: String) : StudentScreen
    data class QuizPlay(val id: Long, val durationMinutes: Int) : StudentScreen
}

class StudentDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge support
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Make system bars completely transparent
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false // White status text
        insetsController.isAppearanceLightNavigationBars = true // Dark navigation icons
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF102B5E),
                    onPrimary = androidx.compose.ui.graphics.Color.White,
                    primaryContainer = androidx.compose.ui.graphics.Color(0xFFF0F5FD),
                    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF102B5E),
                    secondary = androidx.compose.ui.graphics.Color(0xFF1D4ED8),
                    onSecondary = androidx.compose.ui.graphics.Color.White,
                    background = androidx.compose.ui.graphics.Color(0xFFF8FAFD),
                    onBackground = androidx.compose.ui.graphics.Color(0xFF1F2937),
                    surface = androidx.compose.ui.graphics.Color.White,
                    onSurface = androidx.compose.ui.graphics.Color(0xFF1F2937),
                    surfaceVariant = androidx.compose.ui.graphics.Color.White,
                    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF4B5563),
                    surfaceContainer = androidx.compose.ui.graphics.Color.White,
                    surfaceContainerHigh = androidx.compose.ui.graphics.Color.White,
                    outline = androidx.compose.ui.graphics.Color(0xFF9CA3AF)
                )
            ) {
                val context = LocalContext.current
                val sessionManager = remember { SessionManager(context) }
                val token = remember { sessionManager.getToken() }
            
            var currentScreen by remember { mutableStateOf<StudentScreen>(StudentScreen.Home) }
            
            // Shared stack or simple history to go back
            val screenHistory = remember { mutableStateListOf<StudentScreen>() }
            
            val navigateTo: (StudentScreen) -> Unit = { screen ->
                screenHistory.add(currentScreen)
                currentScreen = screen
            }
            
            val navigateBack: () -> Unit = {
                if (screenHistory.isNotEmpty()) {
                    val last = screenHistory.removeAt(screenHistory.size - 1)
                    currentScreen = last
                } else {
                    // Close the activity if no history
                    finish()
                }
            }
            
            // Intercept Back Press
            BackHandler {
                navigateBack()
            }

            // ViewModels initialization
            val homeVm: StudentHomeViewModel = viewModel()
            val subjectVm: StudentSubjectViewModel = viewModel()
            val contentVm: StudentContentViewModel = viewModel()
            val notifVm: StudentNotificationViewModel = viewModel()
            val profileVm: StudentProfileViewModel = viewModel()

            // Fetch student profile dynamically on startup
            LaunchedEffect(key1 = token) {
                homeVm.fetchDashboard(token)
                notifVm.fetchNotifications(token, com.pab.digitallearning.util.StudentNotificationPrefs(context))
            }

            val homeState by homeVm.homeState.collectAsState()
            val profile = (homeState as? com.pab.digitallearning.ui.student.home.StudentHomeUiState.Success)?.data?.profile

            val notifState by notifVm.notificationsState.collectAsState()
            val hasUnreadNotifications = (notifState as? com.pab.digitallearning.ui.student.notification.StudentNotificationUiState.Success)
                ?.data?.any { !it.isRead } ?: false

            // Define active page level & title
            val (level, title) = when (val screen = currentScreen) {
                is StudentScreen.Home -> 1 to "Beranda"
                is StudentScreen.Subjects -> 1 to "Mata Pelajaran"
                is StudentScreen.Notifications -> 1 to "Notifikasi"
                is StudentScreen.Profile -> 1 to "Profil Saya"
                is StudentScreen.SubjectDetail -> 2 to screen.name
                is StudentScreen.ContentDetail -> 3 to screen.title
                is StudentScreen.QuizPlay -> 4 to "Pengerjaan Kuis"
            }

            val showGlobalTopBar = currentScreen !is StudentScreen.QuizPlay

            Scaffold(
                topBar = {
                    if (showGlobalTopBar) {
                        PremiumTopHeader(
                            profile = profile,
                            level = level,
                            title = title,
                            onBackClick = navigateBack,
                            onRefreshClick = {
                                when (currentScreen) {
                                    is StudentScreen.Home -> homeVm.fetchDashboard(token)
                                    is StudentScreen.Subjects -> subjectVm.fetchSubjects(token)
                                    is StudentScreen.Notifications -> notifVm.fetchNotifications(token, com.pab.digitallearning.util.StudentNotificationPrefs(context))
                                    is StudentScreen.Profile -> profileVm.fetchProfile(token)
                                    else -> {}
                                }
                            },
                            onNotificationsClick = {
                                screenHistory.clear()
                                currentScreen = StudentScreen.Notifications
                            }
                        )
                    }
                },
                bottomBar = {
                    // Only show bottom navigation on main tabs
                    val showBottomBar = when (currentScreen) {
                        StudentScreen.Home,
                        StudentScreen.Subjects,
                        StudentScreen.Notifications,
                        StudentScreen.Profile -> true
                        else -> false
                    }
                    
                    if (showBottomBar) {
                        NavigationBar(
                            containerColor = androidx.compose.ui.graphics.Color.White,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == StudentScreen.Home,
                                onClick = {
                                    screenHistory.clear()
                                    currentScreen = StudentScreen.Home
                                },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                                label = { Text("Beranda", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = androidx.compose.ui.graphics.Color(0xFF102B5E),
                                    selectedTextColor = androidx.compose.ui.graphics.Color(0xFF102B5E),
                                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFEBF3FC)
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == StudentScreen.Subjects,
                                onClick = {
                                    screenHistory.clear()
                                    currentScreen = StudentScreen.Subjects
                                },
                                icon = { Icon(Icons.Default.List, contentDescription = "Pelajaran") },
                                label = { Text("Pelajaran", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = androidx.compose.ui.graphics.Color(0xFF102B5E),
                                    selectedTextColor = androidx.compose.ui.graphics.Color(0xFF102B5E),
                                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFEBF3FC)
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == StudentScreen.Notifications,
                                onClick = {
                                    screenHistory.clear()
                                    currentScreen = StudentScreen.Notifications
                                },
                                icon = { 
                                    if (currentScreen != StudentScreen.Notifications && hasUnreadNotifications) {
                                        androidx.compose.material3.BadgedBox(badge = { androidx.compose.material3.Badge { Text("") } }) { 
                                            Icon(Icons.Default.Notifications, contentDescription = "Notifikasi") 
                                        }
                                    } else {
                                        Icon(Icons.Default.Notifications, contentDescription = "Notifikasi") 
                                    }
                                },
                                label = { Text("Notifikasi", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = androidx.compose.ui.graphics.Color(0xFF102B5E),
                                    selectedTextColor = androidx.compose.ui.graphics.Color(0xFF102B5E),
                                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFEBF3FC)
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == StudentScreen.Profile,
                                onClick = {
                                    screenHistory.clear()
                                    currentScreen = StudentScreen.Profile
                                },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                                label = { Text("Profil", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = androidx.compose.ui.graphics.Color(0xFF102B5E),
                                    selectedTextColor = androidx.compose.ui.graphics.Color(0xFF102B5E),
                                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFEBF3FC)
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = if (showGlobalTopBar) innerPadding.calculateTopPadding() else 0.dp,
                            bottom = innerPadding.calculateBottomPadding()
                        )
                ) {
                    when (val screen = currentScreen) {
                        is StudentScreen.Home -> {
                            StudentHomeScreen(
                                viewModel = homeVm,
                                token = token,
                                onTaskClick = { task ->
                                    navigateTo(StudentScreen.ContentDetail(task.id, task.judul))
                                },
                                onNotificationsClick = {
                                    navigateTo(StudentScreen.Notifications)
                                }
                            )
                        }
                        is StudentScreen.Subjects -> {
                            StudentSubjectListScreen(
                                viewModel = subjectVm,
                                token = token,
                                onSubjectClick = { sub ->
                                    navigateTo(StudentScreen.SubjectDetail(sub.id, sub.nama))
                                },
                                onInterestChanged = {
                                    // Refresh Home Screen's SAW priorities
                                    homeVm.fetchDashboard(token)
                                }
                            )
                        }
                        is StudentScreen.Notifications -> {
                            StudentNotificationScreen(
                                viewModel = notifVm,
                                token = token,
                                onNotificationClick = { contentId ->
                                    navigateTo(StudentScreen.ContentDetail(contentId, "Detail Notifikasi"))
                                }
                            )
                        }
                        is StudentScreen.Profile -> {
                            StudentProfileScreen(
                                viewModel = profileVm,
                                token = token,
                                onLogoutClick = {
                                    sessionManager.clearSession()
                                    val intent = Intent(this@StudentDashboardActivity, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            )
                        }
                        is StudentScreen.SubjectDetail -> {
                            StudentSubjectDetailScreen(
                                subjectId = screen.id,
                                subjectName = screen.name,
                                viewModel = subjectVm,
                                token = token,
                                onBackClick = navigateBack,
                                onContentClick = { contentItem ->
                                    navigateTo(StudentScreen.ContentDetail(contentItem.id, contentItem.judul))
                                }
                            )
                        }
                        is StudentScreen.ContentDetail -> {
                            StudentContentDetailScreen(
                                contentId = screen.id,
                                judul = screen.title,
                                viewModel = contentVm,
                                token = token,
                                onBackClick = navigateBack,
                                onPlayQuizClick = { contentId, duration ->
                                    navigateTo(StudentScreen.QuizPlay(contentId, duration))
                                }
                            )
                        }
                        is StudentScreen.QuizPlay -> {
                            StudentQuizPlayScreen(
                                contentId = screen.id,
                                durationMinutes = screen.durationMinutes,
                                viewModel = contentVm,
                                token = token,
                                onBackClick = navigateBack,
                                onQuizFinished = {
                                    // Refresh states and go back
                                    homeVm.fetchDashboard(token)
                                    navigateBack()
                                }
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
fun PremiumTopHeader(
    profile: com.pab.digitallearning.data.model.StudentDashboardProfile?,
    level: Int,
    title: String,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = androidx.compose.ui.graphics.Color(0xFF102B5E), // Solid elegant primary navy
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color(0xFF0B1930), // Deep Space Navy
                            androidx.compose.ui.graphics.Color(0xFF153060)  // Royal Blue Accent
                        )
                    )
                )
        ) {
            // Background ambient circle glows - Wrapped in matchParentSize Box to not affect layout height calculation
            Box(
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-40).dp)
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.04f), CircleShape)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Back Button (Arrow Left) - Shown only on Level 2, 3, etc.
                if (level >= 2) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 2. Student Profile Image Circle (Dynamic / Initials Fallback / Skeleton)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f))
                        .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile != null) {
                        if (!profile.fotoProfile.isNullOrBlank()) {
                            AsyncImage(
                                model = profile.fotoProfile,
                                contentDescription = "Foto Profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            val initials = profile.namaLengkap.trim().split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .map { it.firstOrNull()?.toString() ?: "" }
                                .joinToString("")
                                .uppercase()

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(
                                                androidx.compose.ui.graphics.Color(0xFFFFD700), // Gold
                                                androidx.compose.ui.graphics.Color(0xFFFFA500)  // Amber
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials.ifEmpty { "?" },
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    } else {
                        // Skeleton placeholder loading circle
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f))
                        )
                    }
                }

                // 3. Middle Section: Dynamic Title / Student info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (profile != null) {
                        // Greeting or Student Name as subtitle
                        Text(
                            text = if (level == 1) "Selamat Belajar, ${profile.namaLengkap.substringBefore(" ")}" else profile.namaLengkap,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        // Display active menu/screen title prominently in the header
                        Text(
                            text = title,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = LocalTextStyle.current.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.2f),
                                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                    blurRadius = 1f
                                )
                            )
                        )
                    } else {
                        // Loading state skeleton
                        Text(
                            text = "Memuat data...",
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = title,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 4. School Logo (R.drawable.logo)
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp),
                    color = androidx.compose.ui.graphics.Color.White
                ) {
                    Image(
                        painter = painterResource(com.pab.digitallearning.R.drawable.logo),
                        contentDescription = "Logo School",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Divider Line
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f))
                )

                // 5. Action Buttons (Refresh & Bell) - Shown on main tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onRefreshClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (level == 1) {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
