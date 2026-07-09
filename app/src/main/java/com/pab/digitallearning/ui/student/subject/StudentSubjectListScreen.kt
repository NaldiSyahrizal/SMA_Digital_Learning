package com.pab.digitallearning.ui.student.subject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.digitallearning.data.model.StudentSubject
import com.pab.digitallearning.ui.student.components.ItemSubjectCard
import com.pab.digitallearning.ui.student.components.SubjectInterestDialog

@Composable
fun PremiumSubjectHeader(
    subjects: List<StudentSubject>,
    modifier: Modifier = Modifier
) {
    val totalMapel = subjects.size
    val totalJp = subjects.sumOf { it.jamPelajaran }
    val totalUmum = subjects.count { it.kategori.lowercase() == "umum" }
    val totalPilihan = subjects.count { it.kategori.lowercase() == "pilihan" }
    val isAnyInterestUnfilled = subjects.any { it.interestScore == 0 }

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
            // Ambient glowing patterns - matchParentSize doesn't affect parent measurements
            Box(
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-50).dp)
                        .background(Color.White.copy(alpha = 0.03f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = (-20).dp, y = 30.dp)
                        .background(Color.White.copy(alpha = 0.02f), CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Daftar Mata Pelajaran",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ringkasan kurikulum dan beban belajar Anda minggu ini.",
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
                    // Total Mapel
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
                                text = totalMapel.toString(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mata Pelajaran",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Total JP
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
                                text = "${totalJp} JP",
                                color = Color(0xFFFFD700), // Sparkling Gold
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Jam Pelajaran",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Categories Split (Umum / Pilihan)
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
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
                                    text = "${totalUmum}U",
                                    color = Color(0xFF34C759), // Green for Umum
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "|",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${totalPilihan}P",
                                    color = Color(0xFFFF9500), // Orange for Pilihan
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Umum & Pilihan",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Unfilled interest warning banner
                if (isAnyInterestUnfilled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFF9500).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFF9500).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Tentukan minat belajar untuk semua mata pelajaran terlebih dahulu agar sistem dapat menghitung prioritas belajar Anda dengan akurat.",
                                color = Color(0xFFFFBD59), // Soft amber readable text
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
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
fun StudentSubjectListScreen(
    viewModel: StudentSubjectViewModel,
    token: String?,
    onSubjectClick: (StudentSubject) -> Unit,
    onInterestChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectsState by viewModel.subjectsState.collectAsState()
    var selectedSubjectForInterest by remember { mutableStateOf<StudentSubject?>(null) }

    LaunchedEffect(key1 = token) {
        viewModel.fetchSubjects(token)
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = subjectsState) {
                is StudentSubjectsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF102B5E))
                    }
                }
                is StudentSubjectsUiState.Success -> {
                    val subjects = state.data

                    if (subjects.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📚",
                                fontSize = 50.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tidak Ada Mata Pelajaran",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF102B5E),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kelas Anda belum dipasangkan dengan paket mata pelajaran apapun.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 1. Full-width Premium Header
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                PremiumSubjectHeader(subjects = subjects)
                            }

                            // 2. Grid Items
                            items(subjects) { subject ->
                                ItemSubjectCard(
                                    subject = subject,
                                    onClick = { onSubjectClick(subject) },
                                    onInterestClick = {
                                        selectedSubjectForInterest = subject
                                    }
                                )
                            }
                        }
                    }
                }
                is StudentSubjectsUiState.Error -> {
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
                            onClick = { viewModel.fetchSubjects(token) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E))
                        ) {
                            Text("Coba Lagi", color = Color.White)
                        }
                    }
                }
            }

            // Interest Rating dialog
            selectedSubjectForInterest?.let { subject ->
                SubjectInterestDialog(
                    subjectName = subject.nama,
                    initialInterest = subject.interestScore,
                    onDismiss = { selectedSubjectForInterest = null },
                    onConfirm = { score ->
                        viewModel.updateInterest(token, subject.id, score) {
                            onInterestChanged() // notify callback to refresh Home screen's SAW
                        }
                        selectedSubjectForInterest = null
                    }
                )
            }
        }
    }
}
