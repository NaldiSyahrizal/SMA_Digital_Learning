package com.pab.digitallearning.ui.teacher.dashboard

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.digitallearning.R
import com.pab.digitallearning.core.SessionManager
import com.pab.digitallearning.data.model.RecentContent
import com.pab.digitallearning.data.model.TeacherDashboardData

@Composable
fun TeacherDashboardScreen(
    viewModel: TeacherDashboardViewModel,
    navController: androidx.navigation.NavController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = remember { sessionManager.getToken() }
    val dashboardState by viewModel.dashboardState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchDashboardStats(token)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFD)) // Soft background
    ) {
        when (val state = dashboardState) {
            is TeacherDashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF102B5E))
                }
            }
            is TeacherDashboardUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.message, color = Color.Red, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchDashboardStats(token) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E))
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }
            is TeacherDashboardUiState.Success -> {
                val data = state.data
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Item 1: Welcome Gradient Banner
                    item {
                        WelcomeBanner()
                    }

                    // Item 2: Key Metrics Title
                    item {
                        Text(
                            text = "Ringkasan Akademik Anda",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }

                    // Item 3: Key Metrics Grid (Classes, Students, Subjects, Contents)
                    item {
                        MetricsGrid(data = data)
                    }

                    // Item 4: Content Types Breakdown Card
                    item {
                        ContentBreakdown(data = data)
                    }

                    // Item 5: Quick Actions Title & Action Cards
                    item {
                        Column {
                            Text(
                                text = "Aksi Cepat",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                QuickActionCard(
                                    title = "Kelola Kelas",
                                    subtitle = "Atur kuis & tugas",
                                    icon = Icons.Default.List,
                                    iconBgColor = Color(0xFF3B82F6).copy(alpha = 0.15f),
                                    iconColor = Color(0xFF3B82F6),
                                    badgeCount = 0,
                                    modifier = Modifier.weight(1f),
                                    onClick = { navController.navigate("content") }
                                )

                                QuickActionCard(
                                    title = "Notifikasi",
                                    subtitle = "Lihat info terbaru",
                                    icon = Icons.Default.Notifications,
                                    iconBgColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                                    iconColor = Color(0xFFEF4444),
                                    badgeCount = data.unreadNotifications,
                                    modifier = Modifier.weight(1f),
                                    onClick = { navController.navigate("notification") }
                                )
                            }
                        }
                    }

                    // Item 6: Recent Activity Section
                    item {
                        Text(
                            text = "Aktivitas Konten Terkini",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (data.recentContents.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Belum ada konten pembelajaran yang dibuat.",
                                            fontSize = 13.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(data.recentContents, key = { it.id }) { content ->
                            RecentContentCard(content = content)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF102B5E), // Deep Admin Blue
                            Color(0xFF1D4ED8)  // Modern Blue
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selamat Datang kembali,",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Guru Hebat!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }

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

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "\"Pendidik adalah penuntun peradaban. Mari wujudkan ekosistem digital belajar mengajar yang interaktif dan menyenangkan hari ini.\"",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun MetricsGrid(data: TeacherDashboardData) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                title = "Kelas Diampu",
                value = "${data.totalClasses}",
                icon = Icons.Default.Home,
                iconColor = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f).fillMaxHeight()
            )

            MetricCard(
                title = "Total Siswa",
                value = "${data.totalStudents}",
                icon = Icons.Default.Person,
                iconColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                title = "Mata Pelajaran",
                value = "${data.totalSubjects}",
                icon = Icons.Default.Star,
                iconColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f).fillMaxHeight()
            )

            MetricCard(
                title = "Konten Pembelajaran",
                value = "${data.totalContents}",
                icon = Icons.Default.Edit,
                iconColor = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1F2937)
            )
            
            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ContentBreakdown(data: TeacherDashboardData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Pembagian Tipe Konten",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BreakdownItem(
                    label = "Materi",
                    count = data.totalMaterials,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )

                BreakdownItem(
                    label = "Tugas",
                    count = data.totalAssignments,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )

                BreakdownItem(
                    label = "Kuis",
                    count = data.totalQuizzes,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BreakdownItem(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = "$count",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = color,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    badgeCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Optional Red unread count badge on Quick Action Notifikasi
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, end = 16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "$badgeCount",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Surface(
                    shape = CircleShape,
                    color = iconBgColor,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF102B5E)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun RecentContentCard(content: RecentContent) {
    val (tipeLabel, tipeColor) = when (content.tipe) {
        "materi" -> Pair("Materi", Color(0xFF10B981))
        "tugas" -> Pair("Tugas", Color(0xFF3B82F6))
        else -> Pair("Kuis", Color(0xFFF59E0B))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tipeColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val icon = when (content.tipe) {
                        "materi" -> Icons.Default.Home
                        "tugas" -> Icons.Default.Edit
                        else -> Icons.Default.Check
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tipeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tipeColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = tipeLabel,
                            color = tipeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    val displayTime = content.createdAt
                        .replace("T", " ")
                        .take(10)
                    Text(
                        text = displayTime,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = content.judul,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Kelas: ${content.className} - Mapel: ${content.subjectName}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
