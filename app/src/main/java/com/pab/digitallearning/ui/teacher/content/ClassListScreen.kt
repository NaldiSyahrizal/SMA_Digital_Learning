package com.pab.digitallearning.ui.teacher.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.digitallearning.core.SessionManager
import com.pab.digitallearning.data.model.TeacherClassroom

@Composable
fun ClassListScreen(
    viewModel: TeacherContentViewModel,
    onClassSelected: (classId: Long, className: String, subjectId: Long, subjectName: String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val classesState by viewModel.classesState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchClasses(sessionManager.getToken())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFD)) // Soft premium background
    ) {
        when (val state = classesState) {
            is ClassesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF102B5E))
                }
            }
            is ClassesUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchClasses(sessionManager.getToken()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E))
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }
            is ClassesUiState.Success -> {
                if (state.classes.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Anda belum di-ploting ke kelas mana pun.",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Silakan hubungi Admin untuk melakukan ploting kelas diampu.",
                            color = Color.Gray.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    var searchQuery by remember { mutableStateOf("") }
                    val filteredClasses = remember(state.classes, searchQuery) {
                        state.classes.filter {
                            it.className.contains(searchQuery, ignoreCase = true) ||
                            it.subjectName.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    val totalClasses = remember(state.classes) {
                        state.classes.map { it.classId }.distinct().size
                    }
                    val totalSubjects = remember(state.classes) {
                        state.classes.map { it.subjectId }.distinct().size
                    }
                    val totalStudents = remember(state.classes) {
                        state.classes.distinctBy { it.classId }.sumOf { it.totalStudents }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            TeacherHeaderCard(
                                totalClasses = totalClasses,
                                totalSubjects = totalSubjects,
                                totalStudents = totalStudents,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = "Daftar Kelas Diampu",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827),
                                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                            )
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari kelas atau mata pelajaran...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Cari",
                                        tint = Color.Gray
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF102B5E),
                                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                                )
                            )
                        }

                        if (filteredClasses.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Tidak ditemukan kelas yang cocok.",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(filteredClasses) { classroom ->
                                ClassroomCard(classroom = classroom) {
                                    onClassSelected(
                                        classroom.classId,
                                        classroom.className,
                                        classroom.subjectId,
                                        classroom.subjectName
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassroomCard(classroom: TeacherClassroom, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFEBF3FC)
                        )
                    )
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Class Icon Badge (glowing effect)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF102B5E),
                modifier = Modifier.size(52.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = classroom.className,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF102B5E)
                )
                Text(
                    text = classroom.subjectName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4B5563),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${classroom.totalStudents} Siswa",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color(0xFF102B5E),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TeacherHeaderCard(
    totalClasses: Int,
    totalSubjects: Int,
    totalStudents: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF102B5E), // Primary Royal Navy
                            Color(0xFF1D4ED8)  // Vibrant Blue
                        )
                    )
                )
                .padding(24.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ringkasan Mengajar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Pantau aktivitas kelas & siswa Anda",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Custom elegant divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    icon = Icons.Default.Home,
                    value = totalClasses.toString(),
                    label = "Kelas Diampu",
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = Icons.Default.List,
                    value = totalSubjects.toString(),
                    label = "Mata Pelajaran",
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = Icons.Default.Person,
                    value = totalStudents.toString(),
                    label = "Total Siswa",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon circular background
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

