package com.pab.digitallearning.ui.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pab.digitallearning.core.SessionManager
import com.pab.digitallearning.data.model.PrincipalDashboardData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalDashboardScreen(
    navController: NavController,
    viewModel: PrincipalDashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchDashboard(sessionManager.getToken())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Dashboard Kepala Sekolah", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Digital Learning (Praktikum)", color = Color.White.copy(alpha=0.8f), fontSize = 12.sp)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF102B5E))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PrincipalDashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF102B5E))
                }
                is PrincipalDashboardUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is PrincipalDashboardUiState.Success -> {
                    DashboardContent(data = state.data)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(data: PrincipalDashboardData) {
    var showChartFor by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Ringkasan Aktivitas Belajar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF102B5E))
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Total Tugas",
                    value = data.totalTugas.toString(),
                    icon = Icons.Default.Assignment,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    onClick = { showChartFor = "Tugas" }
                )
                StatCard(
                    title = "Total Kuis",
                    value = data.totalKuis.toString(),
                    icon = Icons.Default.Quiz,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                    onClick = { showChartFor = "Kuis" }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Guru Aktif",
                    value = "${data.teacherParticipation.active} / ${data.teacherParticipation.total}",
                    icon = Icons.Default.School,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Siswa Aktif",
                    value = "${data.studentParticipation.active} / ${data.studentParticipation.total}",
                    icon = Icons.Default.People,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Minat Mapel (Partisipasi Tertinggi)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF102B5E))
        }

        if (data.subjectInterests.isEmpty()) {
            item {
                Text("Belum ada data partisipasi", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            items(data.subjectInterests) { interest ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(interest.subjectName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(interest.subjectName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${interest.avgRating} / 5.0", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                                Text(" (${interest.voters} siswa menilai)", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showChartFor != null) {
        val chartData = if (showChartFor == "Tugas") data.tugasStats else data.kuisStats
        AlertDialog(
            onDismissRequest = { showChartFor = null },
            title = { Text(text = "Statistik Waktu - $showChartFor", fontWeight = FontWeight.Bold) },
            text = {
                SimpleBarChart(data = chartData)
            },
            confirmButton = {
                TextButton(onClick = { showChartFor = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Card(
        onClick = { onClick?.invoke() },
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SimpleBarChart(data: List<com.pab.digitallearning.data.model.TimeStatData>?) {
    if (data.isNullOrEmpty()) {
        Text("Tidak ada data statistik waktu", color = Color.Gray)
        return
    }
    
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { stat ->
            val heightFraction = stat.count.toFloat() / maxCount.toFloat()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(text = stat.count.toString(), fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(fraction = heightFraction.coerceAtLeast(0.01f))
                        .background(Color(0xFF102B5E), shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stat.label, fontSize = 10.sp, color = Color.DarkGray)
            }
        }
    }
}
