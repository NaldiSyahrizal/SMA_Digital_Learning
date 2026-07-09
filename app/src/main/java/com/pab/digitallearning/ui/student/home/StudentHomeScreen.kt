package com.pab.digitallearning.ui.student.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Star
import com.pab.digitallearning.data.model.StudentPriorityTask
import com.pab.digitallearning.ui.student.components.TaskPriorityItemCard
import java.util.Calendar

@Composable
fun PremiumWelcomeHeader(
    profile: com.pab.digitallearning.data.model.StudentDashboardProfile?,
    modifier: Modifier = Modifier
) {
    val sapaan = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..17 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    val quote = remember {
        val quotes = listOf(
            "\"Pendidikan adalah senjata paling mematikan di dunia, karena dengan itu Anda bisa mengubah dunia.\" — Nelson Mandela",
            "\"Cara terbaik untuk memprediksi masa depan adalah dengan menciptakannya.\" — Peter Drucker",
            "\"Fokuslah pada progres belajarmu hari ini, sekecil apapun itu. Setiap langkah membawamu lebih dekat ke mimpimu!\"",
            "\"Jangan takut gagal. Kegagalan adalah kesempatan untuk memulai lagi dengan lebih cerdas.\" — Henry Ford",
            "\"Selesaikan tugas terpentingmu hari ini agar akhir pekanmu lebih tenang dan menyenangkan! 📚\"",
            "\"Pendidikan bukan cuma pergi ke sekolah dan mendapatkan gelar, tapi juga soal memperluas cakupan ilmu pengetahuan dan menyerap ilmu kehidupan.\" — Shakuntala Devi"
        )
        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        quotes[day % quotes.size]
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102B5E)),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0B1930), // Deep Space Navy
                            Color(0xFF153060)  // Royal Blue Accent
                        )
                    )
                )
        ) {
            // Ambient glowing circles for premium styling - matchParentSize doesn't affect parent measurements
            Box(
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 40.dp, y = (-60).dp)
                        .background(Color.White.copy(alpha = 0.03f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = (-30).dp, y = 40.dp)
                        .background(Color.White.copy(alpha = 0.02f), CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (profile != null) "$sapaan, ${profile.namaLengkap.substringBefore(" ")}!" else "$sapaan!",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ayo raih prestasi terbaikmu lewat belajar!",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Inspirational Quote Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Kutipan Harian",
                                color = Color(0xFFFFD700), // Sparkling Gold
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = quote,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    viewModel: StudentHomeViewModel,
    token: String?,
    onTaskClick: (StudentPriorityTask) -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeState by viewModel.homeState.collectAsState()

    // Load data initially
    LaunchedEffect(key1 = token) {
        viewModel.fetchDashboard(token)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
    ) {
        when (val state = homeState) {
            is StudentHomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF102B5E))
                }
            }
            is StudentHomeUiState.Success -> {
                val dashboardData = state.data
                val stats = dashboardData.stats
                val priorities = dashboardData.priorities

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Premium Welcome & Quotes Header
                    item {
                        PremiumWelcomeHeader(profile = dashboardData.profile)
                    }

                    // 2. Stats Metrics row
                    item {
                        StatsRow(
                            averageGrade = stats.averageGrade,
                            completed = stats.completedTasks,
                            pending = stats.totalPendingTasks
                        )
                    }

                    // 3. Learning Recommendation Section title
                    item {
                        Text(
                            text = "Rekomendasi Belajar Untukmu",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF102B5E)
                        )
                    }

                    // 4. Priorities list
                    if (priorities.isEmpty()) {
                        item {
                            EmptyPrioritiesCard()
                        }
                    } else {
                        items(priorities) { task ->
                            TaskPriorityItemCard(
                                task = task,
                                onClick = { onTaskClick(task) }
                            )
                        }
                    }

                    // Extra space at bottom
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
            is StudentHomeUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchDashboard(token) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E))
                    ) {
                        Text("Coba Lagi", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatsRow(averageGrade: Double, completed: Int, pending: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            title = "Rata-rata",
            value = String.format(java.util.Locale.US, "%.1f", averageGrade),
            containerColor = Color(0xFFEBF3FC),
            textColor = Color(0xFF007AFF),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Selesai",
            value = completed.toString(),
            containerColor = Color(0xFFEAF9EE),
            textColor = Color(0xFF34C759),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Pending",
            value = pending.toString(),
            containerColor = Color(0xFFFFF2EB),
            textColor = Color(0xFFFF9500),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
        }
    }
}



@Composable
fun EmptyPrioritiesCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFEAF9EE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✅",
                    fontSize = 28.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Semua Tugas Selesai!",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF102B5E),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hebat! Anda tidak memiliki tugas atau kuis tertunda untuk saat ini.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
