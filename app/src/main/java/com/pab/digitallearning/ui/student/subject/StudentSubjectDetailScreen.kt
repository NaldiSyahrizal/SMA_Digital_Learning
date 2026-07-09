package com.pab.digitallearning.ui.student.subject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.digitallearning.data.model.StudentContentItem
import com.pab.digitallearning.util.DateTimeUtils

@Composable
fun PremiumContentHeader(
    contents: List<StudentContentItem>,
    modifier: Modifier = Modifier
) {
    val totalKonten = contents.size
    val totalMateri = contents.count { it.tipe.lowercase() == "materi" }
    val totalTugas = contents.count { it.tipe.lowercase() == "tugas" }
    val totalKuis = contents.count { it.tipe.lowercase() == "kuis" }
    val totalBelumDikerjakan = contents.count {
        it.tipe.lowercase() != "materi" && it.submissionStatus.lowercase() == "not_submitted"
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
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0B1930), // Deep Space Navy
                            Color(0xFF153060)  // Royal Blue Accent
                        )
                    )
                )
        ) {
            // Ambient glowing circles - matchParentSize doesn't affect parent measurements
            Box(
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 35.dp, y = (-55).dp)
                        .background(Color.White.copy(alpha = 0.03f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = (-25).dp, y = 35.dp)
                        .background(Color.White.copy(alpha = 0.02f), CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Aktivitas Belajar",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Pantau progres belajar dan tugas aktif Anda.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Konten
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = totalKonten.toString(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Total Konten",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Split Types (Materi / Tugas / Kuis)
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${totalMateri}M",
                                    color = Color(0xFF007AFF), // Blue for Materi
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "•",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${totalTugas}T",
                                    color = Color(0xFFFF9500), // Orange for Tugas
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "•",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${totalKuis}K",
                                    color = Color(0xFF34C759), // Green for Kuis
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Materi • Tugas • Kuis",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Belum Dikerjakan
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .background(
                                if (totalBelumDikerjakan > 0) Color(0xFFFF3B30).copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                if (totalBelumDikerjakan > 0) Color(0xFFFF3B30).copy(alpha = 0.3f)
                                else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = totalBelumDikerjakan.toString(),
                                color = if (totalBelumDikerjakan > 0) Color(0xFFFF453A) else Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Belum Selesai",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSubjectDetailScreen(
    subjectId: Long,
    subjectName: String,
    viewModel: StudentSubjectViewModel,
    token: String?,
    onBackClick: () -> Unit,
    onContentClick: (StudentContentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentsState by viewModel.contentsState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Materi", "Tugas", "Kuis")

    LaunchedEffect(key1 = subjectId, key2 = token) {
        viewModel.fetchContents(token, subjectId)
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Premium Content Summary Header (Fixed at the top, above the TabRow)
            val subjectsList = (contentsState as? StudentContentsUiState.Success)?.data ?: emptyList()
            PremiumContentHeader(
                contents = subjectsList,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            )

            // 2. Tab Header Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF102B5E),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF102B5E)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (val state = contentsState) {
                    is StudentContentsUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF102B5E))
                        }
                    }
                    is StudentContentsUiState.Success -> {
                        // Filter content items based on tab
                        val filteredItems = state.data.filter { item ->
                            when (selectedTab) {
                                0 -> item.tipe.lowercase() == "materi"
                                1 -> item.tipe.lowercase() == "tugas"
                                else -> item.tipe.lowercase() == "kuis"
                            }
                        }

                        if (filteredItems.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "📖"
                                        1 -> "📝"
                                        else -> "✏️"
                                    },
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Belum Ada ${tabs[selectedTab]}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF102B5E),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Guru belum mengunggah berkas ${tabs[selectedTab].lowercase()} untuk pelajaran ini.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 2. List Items
                                items(filteredItems) { item ->
                                    StudentContentItemCard(
                                        item = item,
                                        onClick = { onContentClick(item) }
                                    )
                                }
                            }
                        }
                    }
                    is StudentContentsUiState.Error -> {
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
                                onClick = { viewModel.fetchContents(token, subjectId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E))
                            ) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentContentItemCard(
    item: StudentContentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val countdown = DateTimeUtils.getCountdownString(item.dueDate)
    val isOverdue = countdown.contains("Terlewati", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.judul,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF102B5E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Optional Badge score if graded
                if (item.submissionStatus.lowercase() == "graded" && item.bestScore != null) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEAF9EE), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Nilai: ${item.bestScore}",
                            color = Color(0xFF34C759),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body Description
            Text(
                text = item.deskripsi,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (item.tipe.lowercase() != "materi") {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Footer deadline + status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Deadline
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = if (isOverdue) Color(0xFFFF3B30) else Color(0xFF007AFF),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = countdown,
                            color = if (isOverdue) Color(0xFFFF3B30) else Color(0xFF102B5E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Submission status string
                    val (statusText, statusColor) = when (item.submissionStatus.lowercase()) {
                        "graded" -> Pair("Sudah Dinilai", Color(0xFF34C759))
                        "submitted" -> Pair("Sudah Dikumpul", Color(0xFF007AFF))
                        else -> {
                            if (isOverdue) Pair("Terlambat", Color(0xFFFF3B30))
                            else Pair("Belum Dikumpul", Color(0xFFFF9500))
                        }
                    }

                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
