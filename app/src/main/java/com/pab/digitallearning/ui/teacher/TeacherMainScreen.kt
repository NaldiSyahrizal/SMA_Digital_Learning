package com.pab.digitallearning.ui.teacher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pab.digitallearning.R
import androidx.compose.ui.layout.ContentScale


@Composable
fun TeacherMainScreen(
    profileViewModel: com.pab.digitallearning.ui.teacher.profile.TeacherProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    contentViewModel: com.pab.digitallearning.ui.teacher.content.TeacherContentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    notificationViewModel: com.pab.digitallearning.ui.teacher.notification.TeacherNotificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    dashboardViewModel: com.pab.digitallearning.ui.teacher.dashboard.TeacherDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val uiState by profileViewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.pab.digitallearning.core.SessionManager(context) }

    LaunchedEffect(Unit) {
        profileViewModel.fetchProfile(sessionManager.getToken())
        notificationViewModel.fetchNotifications(sessionManager.getToken())
    }

    val notificationUiState by notificationViewModel.notificationsState.collectAsState()
    val hasUnreadNotifications = (notificationUiState as? com.pab.digitallearning.ui.teacher.notification.NotificationUiState.Success)
        ?.notifications?.any { !it.isRead } ?: false

    Scaffold(
        topBar = {
            TeacherTopBar(
                currentRoute = currentRoute ?: TeacherRoute.Dashboard.route,
                arguments = navBackStackEntry?.arguments,
                uiState = uiState,
                onBackPressed = { navController.popBackStack() }
            )
        },
        bottomBar = {
            val showBottomBar = currentRoute in listOf(
                TeacherRoute.Dashboard.route,
                TeacherRoute.Content.route,
                TeacherRoute.Notification.route,
                TeacherRoute.Profile.route
            )
            if (showBottomBar) {
                TeacherBottomNavigation(navController = navController, currentRoute = currentRoute, hasUnreadNotifications = hasUnreadNotifications)
            }
        },
        containerColor = Color(0xFFF8FAFD) // Background cerah
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            TeacherNavHost(
                navController = navController,
                profileViewModel = profileViewModel,
                contentViewModel = contentViewModel,
                notificationViewModel = notificationViewModel,
                dashboardViewModel = dashboardViewModel
            )
        }
    }
}

@Composable
fun TeacherTopBar(
    currentRoute: String,
    arguments: android.os.Bundle?,
    uiState: com.pab.digitallearning.ui.teacher.profile.ProfileUiState,
    onBackPressed: () -> Unit
) {
    val isDetailScreen = currentRoute.startsWith("class_detail") ||
                         currentRoute.startsWith("grading") ||
                         currentRoute.startsWith("add_content") ||
                         currentRoute.startsWith("content_detail") ||
                         currentRoute.startsWith("edit_content")

    val className = arguments?.getString("className") ?: "Detail Kelas"
    val subjectName = arguments?.getString("subjectName") ?: ""
    val contentTitle = arguments?.getString("contentTitle") ?: "Penilaian"

    val nama = if (uiState is com.pab.digitallearning.ui.teacher.profile.ProfileUiState.Success) {
        uiState.profile.namaLengkap ?: "Guru"
    } else {
        "Memuat..."
    }

    // Header melengkung gemoy
    Surface(
        color = Color(0xFF102B5E), // Warna biru tua admin
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        shadowElevation = 8.dp
    ) {
        if (isDetailScreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .statusBarsPadding(), // Support edge-to-edge
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button Icon
                    IconButton(onClick = onBackPressed, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        when {
                            currentRoute.startsWith("class_detail") -> {
                                Text(
                                    text = className,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (subjectName.isNotEmpty()) {
                                    Text(
                                        text = subjectName,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            currentRoute.startsWith("add_content") -> {
                                Text(
                                    text = "Buat Konten Baru",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$className - $subjectName",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            currentRoute.startsWith("edit_content") -> {
                                Text(
                                    text = "Edit Konten",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$className - $subjectName",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            currentRoute.startsWith("content_detail") -> {
                                Text(
                                    text = "Detail Konten",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$className - $subjectName",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            else -> { // starts with "grading"
                                Text(
                                    text = "Penilaian Tugas",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = contentTitle,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Logo SMA Placeholder (lingkaran putih)
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .statusBarsPadding(), // Support edge-to-edge
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Profile & Greeting (diberi weight agar tidak mendesak logo kanan)
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Foto Profil Gemoy
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(56.dp)
                    ) {
                        val fotoProfile = if (uiState is com.pab.digitallearning.ui.teacher.profile.ProfileUiState.Success) {
                            uiState.profile.fotoProfile
                        } else {
                            null
                        }

                        if (!fotoProfile.isNullOrEmpty()) {
                            coil.compose.AsyncImage(
                                model = fotoProfile,
                                contentDescription = "Profile Picture",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFF102B5E),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Column teks diberi weight agar Marquee bekerja di batas yang pas
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Halo, Guru Hebat!",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = nama,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Logo SMA Placeholder (lingkaran putih)
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherBottomNavigation(navController: NavHostController, currentRoute: String?, hasUnreadNotifications: Boolean = false) {
    val items = listOf(
        TeacherRoute.Dashboard to Icons.Default.Home,
        TeacherRoute.Content to Icons.Default.List,
        TeacherRoute.Notification to Icons.Default.Notifications,
        TeacherRoute.Profile to Icons.Default.Person
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        items.forEach { (route, icon) ->
            NavigationBarItem(
                icon = { 
                    if (route == TeacherRoute.Notification && hasUnreadNotifications) {
                        BadgedBox(badge = { Badge { Text("") } }) {
                            Icon(icon, contentDescription = route.title)
                        }
                    } else {
                        Icon(icon, contentDescription = route.title) 
                    }
                },
                label = { Text(route.title) },
                selected = currentRoute == route.route,
                onClick = {
                    navController.navigate(route.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF102B5E),
                    selectedTextColor = Color(0xFF102B5E),
                    indicatorColor = Color(0xFFE3F2FD) // Warna indikator gemoy
                )
            )
        }
    }
}

@Composable
fun TeacherNavHost(
    navController: NavHostController,
    profileViewModel: com.pab.digitallearning.ui.teacher.profile.TeacherProfileViewModel,
    contentViewModel: com.pab.digitallearning.ui.teacher.content.TeacherContentViewModel,
    notificationViewModel: com.pab.digitallearning.ui.teacher.notification.TeacherNotificationViewModel,
    dashboardViewModel: com.pab.digitallearning.ui.teacher.dashboard.TeacherDashboardViewModel
) {
    NavHost(
        navController = navController,
        startDestination = TeacherRoute.Dashboard.route
    ) {
        composable(TeacherRoute.Dashboard.route) {
            com.pab.digitallearning.ui.teacher.dashboard.TeacherDashboardScreen(
                viewModel = dashboardViewModel,
                navController = navController
            )
        }
        composable(TeacherRoute.Content.route) {
            com.pab.digitallearning.ui.teacher.content.ClassListScreen(
                viewModel = contentViewModel,
                onClassSelected = { classId, className, subjectId, subjectName ->
                    navController.navigate("class_detail/$classId/$className/$subjectId/$subjectName")
                }
            )
        }
        composable(
            route = "class_detail/{classId}/{className}/{subjectId}/{subjectName}",
            arguments = listOf(
                androidx.navigation.navArgument("classId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("className") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("subjectId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("subjectName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            val className = backStackEntry.arguments?.getString("className") ?: ""
            val subjectId = backStackEntry.arguments?.getLong("subjectId") ?: 0L
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            
            com.pab.digitallearning.ui.teacher.content.ClassDetailScreen(
                viewModel = contentViewModel,
                classId = classId,
                className = className,
                subjectId = subjectId,
                subjectName = subjectName,
                onContentSelected = { contentId ->
                    navController.navigate("content_detail/$contentId/$classId/$className/$subjectId/$subjectName")
                },
                onAddContentClicked = {
                    navController.navigate("add_content/$classId/$className/$subjectId/$subjectName")
                },
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(
            route = "add_content/{classId}/{className}/{subjectId}/{subjectName}",
            arguments = listOf(
                androidx.navigation.navArgument("classId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("className") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("subjectId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("subjectName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            val className = backStackEntry.arguments?.getString("className") ?: ""
            val subjectId = backStackEntry.arguments?.getLong("subjectId") ?: 0L
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            
            com.pab.digitallearning.ui.teacher.content.AddContentScreen(
                viewModel = contentViewModel,
                classId = classId,
                className = className,
                subjectId = subjectId,
                subjectName = subjectName,
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(
            route = "content_detail/{contentId}/{classId}/{className}/{subjectId}/{subjectName}?initialTab={initialTab}",
            arguments = listOf(
                androidx.navigation.navArgument("contentId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("classId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("className") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("subjectId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("subjectName") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("initialTab") {
                    type = androidx.navigation.NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getLong("contentId") ?: 0L
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            val className = backStackEntry.arguments?.getString("className") ?: ""
            val subjectId = backStackEntry.arguments?.getLong("subjectId") ?: 0L
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
            
            com.pab.digitallearning.ui.teacher.content.ContentDetailScreen(
                viewModel = contentViewModel,
                contentId = contentId,
                classId = classId,
                className = className,
                subjectId = subjectId,
                subjectName = subjectName,
                initialTab = initialTab,
                onEditClicked = {
                    navController.navigate("edit_content/$contentId/$classId/$className/$subjectId/$subjectName")
                },
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(
            route = "edit_content/{contentId}/{classId}/{className}/{subjectId}/{subjectName}",
            arguments = listOf(
                androidx.navigation.navArgument("contentId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("classId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("className") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("subjectId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("subjectName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getLong("contentId") ?: 0L
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            val className = backStackEntry.arguments?.getString("className") ?: ""
            val subjectId = backStackEntry.arguments?.getLong("subjectId") ?: 0L
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            
            com.pab.digitallearning.ui.teacher.content.EditContentScreen(
                viewModel = contentViewModel,
                contentId = contentId,
                classId = classId,
                className = className,
                subjectId = subjectId,
                subjectName = subjectName,
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(TeacherRoute.Notification.route) {
            com.pab.digitallearning.ui.teacher.notification.NotificationScreen(
                viewModel = notificationViewModel,
                navController = navController
            )
        }
        composable(TeacherRoute.Profile.route) {
            com.pab.digitallearning.ui.teacher.profile.TeacherProfileScreen(viewModel = profileViewModel)
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
    }
}
