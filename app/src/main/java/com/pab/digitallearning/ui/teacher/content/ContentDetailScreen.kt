package com.pab.digitallearning.ui.teacher.content

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.digitallearning.core.SessionManager
import com.pab.digitallearning.data.model.ClassroomContent
import com.pab.digitallearning.data.model.QuizQuestion
import com.pab.digitallearning.data.model.StudentSubmission
import com.pab.digitallearning.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentDetailScreen(
    viewModel: TeacherContentViewModel,
    contentId: Long,
    classId: Long,
    className: String,
    subjectId: Long,
    subjectName: String,
    initialTab: Int = 0,
    onEditClicked: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    val contentState by viewModel.contentDetailState.collectAsState()
    val submissionsState by viewModel.submissionsState.collectAsState()
    val commentsState by viewModel.commentsState.collectAsState()
    val postCommentLoading by viewModel.postCommentLoading.collectAsState()
    
    var selectedTab by remember { mutableStateOf(initialTab) }
    var selectedSubmission by remember { mutableStateOf<StudentSubmission?>(null) }
    var showGradingDialog by remember { mutableStateOf(false) }
    var activeQuestionIndex by remember { mutableStateOf(0) }
    var submissionSearchQuery by remember { mutableStateOf("") }
    var sortAscending by remember { mutableStateOf(true) }

    LaunchedEffect(contentId) {
        val token = sessionManager.getToken()
        viewModel.fetchContentDetail(token, contentId)
        viewModel.fetchSubmissions(token, contentId)
        viewModel.fetchComments(token ?: "", contentId)
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFD),
        floatingActionButton = {
            // Edit button floating on Tab 1
            if (selectedTab == 0 && contentState is ContentDetailUiState.Success) {
                FloatingActionButton(
                    onClick = onEditClicked,
                    containerColor = Color(0xFF102B5E),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Konten")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = contentState) {
                is ContentDetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF102B5E))
                    }
                }
                is ContentDetailUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
                is ContentDetailUiState.Success -> {
                    val content = state.content
                    val showProgressTab = content.tipe != "materi"

                    if (showProgressTab) {
                        // Display Tabs only for Assignment and Quiz
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.White,
                            contentColor = Color(0xFF102B5E),
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color(0xFF102B5E)
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Detail Konten", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Progres & Penilaian", fontWeight = FontWeight.Bold) }
                            )
                        }
                    } else {
                        selectedTab = 0
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (selectedTab == 0) {
                            // TAB 1: CONTENT DETAIL
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    // Info Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val tipeColor = when (content.tipe) {
                                                    "materi" -> Color(0xFF10B981)
                                                    "tugas" -> Color(0xFF3B82F6)
                                                    else -> Color(0xFFF59E0B)
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = tipeColor.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = content.tipe.replaceFirstChar { it.uppercase() },
                                                        color = tipeColor,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = content.judul,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1F2937)
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = content.deskripsi,
                                                fontSize = 14.sp,
                                                color = Color.Gray,
                                                lineHeight = 20.sp
                                            )

                                            if (content.tipe != "materi") {
                                                val isStrictlyClosed = DateTimeUtils.isStrictlyClosed(content.isClosed)
                                                val isOverdue = DateTimeUtils.isOverdue(content.dueDate)
                                                
                                                if (!content.dueDate.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.Top
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Info,
                                                            contentDescription = null,
                                                            tint = when {
                                                                isStrictlyClosed -> Color.Red.copy(alpha = 0.8f)
                                                                isOverdue -> Color(0xFFFF9500)
                                                                else -> Color(0xFF3B82F6)
                                                            },
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(
                                                                text = "Tenggat Waktu: ${content.dueDate.substringBefore("T")} ${content.dueDate.substringAfter("T").take(5)}",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = Color(0xFF1F2937)
                                                            )
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            val countdown = DateTimeUtils.getCountdownString(content.dueDate)
                                                            val statusText = when {
                                                                isStrictlyClosed -> "Ditutup Manual oleh Guru"
                                                                isOverdue -> "Tenggat Terlewati (Masih Bisa Submit Terlambat)"
                                                                else -> countdown
                                                            }
                                                            val statusColor = when {
                                                                isStrictlyClosed -> Color.Red.copy(alpha = 0.8f)
                                                                isOverdue -> Color(0xFFFF9500)
                                                                else -> Color(0xFF3B82F6)
                                                            }
                                                            Text(
                                                                text = statusText,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = statusColor
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))
                                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text("Status Pengumpulan", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(top = 2.dp)
                                                        ) {
                                                            val badgeColor = when {
                                                                isStrictlyClosed -> Color.Red
                                                                isOverdue -> Color(0xFFFF9500)
                                                                else -> Color(0xFF10B981)
                                                            }
                                                            val badgeText = when {
                                                                isStrictlyClosed -> "DITUTUP (CLOSED)"
                                                                isOverdue -> "TERLAMBAT (LATE)"
                                                                else -> "AKTIF (OPEN)"
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                    .background(badgeColor, CircleShape)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = badgeText,
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Black,
                                                                color = badgeColor
                                                            )
                                                        }
                                                    }
                                                    
                                                    // Toggle button uses ONLY content.isClosed (teacher flag), not isOverdue
                                                    Button(
                                                        onClick = {
                                                            val token = sessionManager.getToken()
                                                            viewModel.toggleContentClose(
                                                                token = token,
                                                                contentId = content.id,
                                                                onResult = { success, msg ->
                                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                                }
                                                            )
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isStrictlyClosed) Color(0xFF10B981) else Color.Red
                                                        ),
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                                        modifier = Modifier.height(36.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isStrictlyClosed) "Buka Akses" else "Tutup Akses",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!content.filePath.isNullOrBlank()) {
                                    item {
                                        // Attachment File Card
                                        val actualFileName = content.filePath.substringAfterLast("/")
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF102B5E), modifier = Modifier.size(28.dp))
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = actualFileName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF1F2937)
                                                        )
                                                        Text("Ketuk untuk mengunduh berkas lampiran", fontSize = 12.sp, color = Color.Gray)
                                                    }
                                                }
                                                IconButton(
                                                    onClick = {
                                                        Toast.makeText(context, "Membuka file...", Toast.LENGTH_SHORT).show()
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(content.filePath))
                                                        context.startActivity(intent)
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Info, contentDescription = "Unduh", tint = Color(0xFF102B5E))
                                                }
                                            }
                                        }
                                }
                            }

                                 // If Quiz, display list of questions with a horizontal number bar
                                 if (content.tipe == "kuis" && content.questions.isNotEmpty()) {
                                     item {
                                         Text(
                                             text = "Daftar Pertanyaan Kuis (${content.questions.size})",
                                             fontWeight = FontWeight.Bold,
                                             fontSize = 16.sp,
                                             color = Color(0xFF102B5E),
                                             modifier = Modifier.padding(top = 8.dp)
                                         )
                                     }

                                     item {
                                         // Horizontal Question Number Bar
                                         androidx.compose.foundation.lazy.LazyRow(
                                             modifier = Modifier
                                                 .fillMaxWidth()
                                                 .padding(vertical = 4.dp),
                                             horizontalArrangement = Arrangement.spacedBy(8.dp),
                                             contentPadding = PaddingValues(horizontal = 2.dp)
                                         ) {
                                             items(content.questions.size) { index ->
                                                 val isSelected = activeQuestionIndex == index
                                                 val containerColor = if (isSelected) Color(0xFF102B5E) else Color.White
                                                 val contentColor = if (isSelected) Color.White else Color(0xFF102B5E)
                                                 val border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF102B5E).copy(alpha = 0.3f))
                                                 
                                                 Surface(
                                                     shape = RoundedCornerShape(10.dp),
                                                     color = containerColor,
                                                     border = border,
                                                     modifier = Modifier
                                                         .size(44.dp)
                                                         .clickable { activeQuestionIndex = index }
                                                 ) {
                                                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                         Text(
                                                             text = "${index + 1}",
                                                             fontWeight = FontWeight.Black,
                                                             color = contentColor,
                                                             fontSize = 14.sp
                                                         )
                                                     }
                                                 }
                                             }
                                         }
                                     }

                                     item {
                                         val q = content.questions.getOrNull(activeQuestionIndex) ?: content.questions.first()
                                         Card(
                                             modifier = Modifier.fillMaxWidth(),
                                             shape = RoundedCornerShape(16.dp),
                                             colors = CardDefaults.cardColors(containerColor = Color.White),
                                             elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                         ) {
                                             Column(modifier = Modifier.padding(16.dp)) {
                                                 Row(
                                                     modifier = Modifier.fillMaxWidth(),
                                                     horizontalArrangement = Arrangement.SpaceBetween,
                                                     verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                     Text(
                                                         text = "Pertanyaan #${activeQuestionIndex + 1} dari ${content.questions.size}",
                                                         fontWeight = FontWeight.Bold,
                                                         color = Color(0xFF102B5E),
                                                         fontSize = 14.sp
                                                     )
                                                 }
                                                 Spacer(modifier = Modifier.height(8.dp))
                                                 Text(
                                                     text = q.pertanyaan,
                                                     fontSize = 15.sp,
                                                     fontWeight = FontWeight.SemiBold,
                                                     color = Color(0xFF1F2937)
                                                 )

                                                 if (!q.imagePath.isNullOrBlank()) {
                                                     Spacer(modifier = Modifier.height(12.dp))
                                                     Box(
                                                         modifier = Modifier
                                                             .fillMaxWidth()
                                                             .height(160.dp)
                                                             .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp)),
                                                         contentAlignment = Alignment.Center
                                                     ) {
                                                         coil.compose.AsyncImage(
                                                             model = q.imagePath,
                                                             contentDescription = "Gambar Pendukung Pertanyaan",
                                                             modifier = Modifier.fillMaxSize(),
                                                             contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                                         )
                                                     }
                                                 }

                                                 Spacer(modifier = Modifier.height(12.dp))

                                                 // List Options (Only for Pilihan Ganda)
                                                 if (q.tipeSoal == "pilihan_ganda") {
                                                     listOf("A" to q.opsiA, "B" to q.opsiB, "C" to q.opsiC, "D" to q.opsiD).forEach { (key, valText) ->
                                                         val isCorrect = q.jawabanBenar == key
                                                         val optionBgColor = if (isCorrect) Color(0xFF10B981).copy(alpha = 0.1f) else Color.Transparent
                                                         val optionBorderColor = if (isCorrect) Color(0xFF10B981) else Color.LightGray.copy(alpha = 0.5f)
                                                         val optionTextColor = if (isCorrect) Color(0xFF10B981) else Color(0xFF1F2937)

                                                         Surface(
                                                             shape = RoundedCornerShape(10.dp),
                                                             color = optionBgColor,
                                                             border = androidx.compose.foundation.BorderStroke(1.dp, optionBorderColor),
                                                             modifier = Modifier
                                                                 .fillMaxWidth()
                                                                 .padding(vertical = 4.dp)
                                                         ) {
                                                             Row(
                                                                 modifier = Modifier
                                                                     .fillMaxWidth()
                                                                     .padding(horizontal = 12.dp, vertical = 10.dp),
                                                                 verticalAlignment = Alignment.CenterVertically
                                                             ) {
                                                                 Text(
                                                                     text = "$key.",
                                                                     fontWeight = FontWeight.Black,
                                                                     color = optionTextColor,
                                                                     fontSize = 14.sp
                                                                 )
                                                                 Spacer(modifier = Modifier.width(8.dp))
                                                                 Text(
                                                                     text = valText ?: "",
                                                                     fontSize = 13.sp,
                                                                     color = optionTextColor,
                                                                     fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                                                     modifier = Modifier.weight(1f)
                                                                 )
                                                                 if (isCorrect) {
                                                                     Icon(
                                                                         imageVector = Icons.Default.CheckCircle,
                                                                         contentDescription = null,
                                                                         tint = Color(0xFF10B981),
                                                                         modifier = Modifier.size(18.dp)
                                                                     )
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 } else {
                                                     Text(
                                                         text = "Tipe Soal: Esai (Jawaban berbentuk teks)",
                                                         fontSize = 13.sp,
                                                         color = Color.Gray,
                                                         fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                         modifier = Modifier.padding(vertical = 8.dp)
                                                     )
                                                 }

                                                 Spacer(modifier = Modifier.height(16.dp))

                                                 // Quick yellow/amber "Koreksi Kesalahan di Soal Ini" button
                                                 Button(
                                                     onClick = onEditClicked,
                                                     colors = ButtonDefaults.buttonColors(
                                                         containerColor = Color(0xFFF59E0B),
                                                         contentColor = Color.White
                                                     ),
                                                     shape = RoundedCornerShape(10.dp),
                                                     modifier = Modifier.fillMaxWidth()
                                                 ) {
                                                     Icon(
                                                         imageVector = Icons.Default.Edit,
                                                         contentDescription = null,
                                                         modifier = Modifier.size(16.dp)
                                                     )
                                                     Spacer(modifier = Modifier.width(8.dp))
                                                     Text(
                                                         text = "Koreksi Kesalahan di Soal Ini",
                                                         fontWeight = FontWeight.Bold,
                                                         fontSize = 13.sp
                                                     )
                                                 }
                                             }
                                         }
                                     }
                                 }
                                 
                                 item {
                                     DiscussionSection(
                                         comments = commentsState,
                                         isLoading = postCommentLoading,
                                         onSendComment = { text, uri ->
                                             val token = sessionManager.getToken() ?: ""
                                             viewModel.postComment(token, contentId, text, uri, context)
                                         },
                                         onEditComment = { commentId, newText ->
                                             val token = sessionManager.getToken() ?: ""
                                             viewModel.editComment(token, contentId, commentId, newText) { success, msg ->
                                                 android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                             }
                                         },
                                         onDeleteComment = { commentId ->
                                             val token = sessionManager.getToken() ?: ""
                                             viewModel.deleteComment(token, contentId, commentId) { success, msg ->
                                                 android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                             }
                                         }
                                     )
                                 }
                             }
                         } else {
                            // TAB 2: PROGRESS & PENILAIAN (SUBMISSIONS & GRADING)
                            when (val subState = submissionsState) {
                                is SubmissionsUiState.Loading -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color(0xFF102B5E))
                                    }
                                }
                                is SubmissionsUiState.Error -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(subState.message, color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                }
                                is SubmissionsUiState.Success -> {
                                    val processedSubmissions = remember(subState.submissions, submissionSearchQuery, sortAscending) {
                                        val filtered = subState.submissions.filter {
                                            it.studentName.contains(submissionSearchQuery, ignoreCase = true) ||
                                            it.nis.contains(submissionSearchQuery, ignoreCase = true)
                                        }
                                        if (sortAscending) {
                                            filtered.sortedBy { it.studentName.lowercase() }
                                        } else {
                                            filtered.sortedByDescending { it.studentName.lowercase() }
                                        }
                                    }

                                    if (subState.submissions.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("Belum ada data pengumpulan siswa.", color = Color.Gray)
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            item {
                                                // Progres Summary Card
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD).copy(alpha = 0.5f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column {
                                                            Text("Progres Pengerjaan Siswa", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                                            Text(
                                                                text = "${content.totalSubmissions} dari ${content.totalStudents} Siswa",
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF102B5E)
                                                            )
                                                        }
                                                        CircularProgressIndicator(
                                                            progress = {
                                                                if (content.totalStudents > 0) {
                                                                    content.totalSubmissions.toFloat() / content.totalStudents.toFloat()
                                                                } else 0f
                                                            },
                                                            color = Color(0xFF102B5E),
                                                            strokeWidth = 6.dp,
                                                            modifier = Modifier.size(44.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            item {
                                                // Search Bar & Sort Toggle Row
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedTextField(
                                                        value = submissionSearchQuery,
                                                        onValueChange = { submissionSearchQuery = it },
                                                        placeholder = { Text("Cari nama/nis siswa...", fontSize = 13.sp) },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp),
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Default.Search,
                                                                contentDescription = null,
                                                                tint = Color.Gray,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        },
                                                        trailingIcon = {
                                                            if (submissionSearchQuery.isNotEmpty()) {
                                                                IconButton(onClick = { submissionSearchQuery = "" }) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Close,
                                                                        contentDescription = "Hapus Pencarian",
                                                                        tint = Color.Gray,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        singleLine = true,
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedContainerColor = Color.White,
                                                            unfocusedContainerColor = Color.White,
                                                            focusedBorderColor = Color(0xFF102B5E),
                                                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f)
                                                        )
                                                    )

                                                    // Sort Toggle Button
                                                    Button(
                                                        onClick = { sortAscending = !sortAscending },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFF102B5E).copy(alpha = 0.1f),
                                                            contentColor = Color(0xFF102B5E)
                                                        ),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                                        modifier = Modifier.height(48.dp)
                                                    ) {
                                                        Text(
                                                            text = if (sortAscending) "A-Z" else "Z-A",
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            }

                                            if (processedSubmissions.isEmpty()) {
                                                item {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(24.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("Siswa tidak ditemukan.", color = Color.Gray)
                                                    }
                                                }
                                            } else {
                                                items(processedSubmissions) { sub ->
                                                    SubmissionItemCard(
                                                        submission = sub,
                                                        onClick = {
                                                            if (sub.status != "not_submitted") {
                                                                selectedSubmission = sub
                                                                showGradingDialog = true
                                                            } else {
                                                                Toast.makeText(context, "Siswa belum mengumpulkan tugas ini.", Toast.LENGTH_SHORT).show()
                                                            }
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
                }
            }
        }

        // Grading Score & Feedback Dialog
        if (showGradingDialog && selectedSubmission != null) {
            val sub = selectedSubmission!!
            GradingDialog(
                submission = sub,
                contentTipe = (contentState as? ContentDetailUiState.Success)?.content?.tipe ?: "",
                questions = (contentState as? ContentDetailUiState.Success)?.content?.questions,
                onDismiss = {
                    showGradingDialog = false
                    selectedSubmission = null
                },
                onGradeSave = { score: Int, feedback: String? ->
                    viewModel.gradeSubmission(
                        token = sessionManager.getToken(),
                        contentId = contentId,
                        submissionId = sub.submissionId!!,
                        nilai = score,
                        catatan = feedback,
                        onResult = { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                showGradingDialog = false
                                selectedSubmission = null
                                // Force refresh content details to update total graded count
                                viewModel.fetchContentDetail(sessionManager.getToken(), contentId)
                            }
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun SubmissionItemCard(submission: StudentSubmission, onClick: () -> Unit) {
    val statusColor = when (submission.status) {
        "graded" -> Color(0xFF10B981) // Emerald Green
        "submitted" -> Color(0xFF3B82F6) // Ocean Blue
        else -> Color.Gray
    }

    val statusLabel = when (submission.status) {
        "graded" -> "Sudah Dinilai"
        "submitted" -> "Menunggu Dinilai"
        else -> "Belum Mengumpulkan"
    }

    val statusIcon = when (submission.status) {
        "graded" -> Icons.Default.Check
        "submitted" -> Icons.Default.Info
        else -> Icons.Default.Warning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = submission.studentName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "NIS: ${submission.nis}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusLabel,
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (submission.exitCount != null && submission.exitCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peringatan",
                            tint = Color(0xFFFF9500),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Terdeteksi Keluar Kuis: ${submission.exitCount} kali",
                            fontSize = 11.sp,
                            color = Color(0xFFFF9500),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Grade score indicator if graded
            if (submission.status == "graded" && submission.nilai != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = submission.nilai.toString(),
                        color = Color(0xFF10B981),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else if (submission.status == "submitted") {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Nilai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradingDialog(
    submission: StudentSubmission,
    contentTipe: String,
    questions: List<QuizQuestion>?,
    onDismiss: () -> Unit,
    onGradeSave: (score: Int, feedback: String?) -> Unit
) {
    var scoreStr by remember { mutableStateOf(submission.nilai?.toString() ?: "") }
    var feedback by remember { mutableStateOf(submission.catatan ?: "") }

    // Calculate MCQ stats
    val mcqQuestions = questions?.filter { it.tipeSoal == "pilihan_ganda" } ?: emptyList()
    var correctCount = 0
    var wrongCount = 0
    mcqQuestions.forEach { q ->
        val ans = submission.quizAnswers?.get(q.id.toString())
        if (ans == q.jawabanBenar) correctCount++ else wrongCount++
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F9FC)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Detail Penilaian",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = submission.studentName,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF102B5E))
                    )
                },
                bottomBar = {
                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = scoreStr,
                                    onValueChange = { scoreStr = it },
                                    label = { Text("Nilai Akhir (0-100)") },
                                    modifier = Modifier.weight(0.35f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = feedback,
                                    onValueChange = { feedback = it },
                                    label = { Text("Catatan Guru") },
                                    modifier = Modifier.weight(0.65f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    val score = scoreStr.toIntOrNull()
                                    if (score != null && score in 0..100) {
                                        onGradeSave(score, if (feedback.isBlank()) null else feedback)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E))
                            ) {
                                Text("Simpan Penilaian", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cheating / Exit warning logs if any
                    if (submission.exitCount != null && submission.exitCount > 0) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFF9500).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFFF9500).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = Color(0xFFFF9500), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Peringatan: Siswa Keluar Kuis", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9500))
                                }
                                Text("Terdeteksi keluar ${submission.exitCount} kali.", fontSize = 12.sp, color = Color(0xFFE65100))
                            }
                        }
                    }

                    if (contentTipe == "kuis" || contentTipe == "ujian") {
                        // Score Summary Header
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("PG Benar", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text("$correctCount", fontSize = 24.sp, color = Color(0xFF34C759), fontWeight = FontWeight.Black)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("PG Salah", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text("$wrongCount", fontSize = 24.sp, color = Color(0xFFFF3B30), fontWeight = FontWeight.Black)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total PG", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text("${mcqQuestions.size}", fontSize = 24.sp, color = Color(0xFF102B5E), fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        if (questions.isNullOrEmpty()) {
                            item { Text("Detail soal tidak tersedia.", color = Color.Gray) }
                        } else {
                            itemsIndexed(questions) { index, q ->
                            val studentAns = submission.quizAnswers?.get(q.id.toString())
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "${index + 1}. ${q.pertanyaan}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (q.tipeSoal == "pilihan_ganda") {
                                        listOf("A" to q.opsiA, "B" to q.opsiB, "C" to q.opsiC, "D" to q.opsiD).forEach { (key, valText) ->
                                            val isStudentChoice = studentAns == key
                                            val isCorrectAnswer = q.jawabanBenar == key

                                            val bgColor = when {
                                                isStudentChoice && isCorrectAnswer -> Color(0xFF34C759).copy(alpha = 0.15f) // Green
                                                isStudentChoice && !isCorrectAnswer -> Color(0xFFFF3B30).copy(alpha = 0.15f) // Red
                                                isCorrectAnswer -> Color(0xFF34C759).copy(alpha = 0.05f) // Light Green hint
                                                else -> Color.Transparent
                                            }
                                            val borderColor = when {
                                                isStudentChoice && isCorrectAnswer -> Color(0xFF34C759)
                                                isStudentChoice && !isCorrectAnswer -> Color(0xFFFF3B30)
                                                isCorrectAnswer -> Color(0xFF34C759).copy(alpha = 0.5f)
                                                else -> Color.LightGray.copy(alpha = 0.5f)
                                            }
                                            val textColor = when {
                                                isStudentChoice && isCorrectAnswer -> Color(0xFF1E823C)
                                                isStudentChoice && !isCorrectAnswer -> Color(0xFFD32F2F)
                                                isCorrectAnswer -> Color(0xFF1E823C)
                                                else -> Color(0xFF1F2937)
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = bgColor,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("$key.", fontWeight = FontWeight.Black, color = textColor)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(valText ?: "", fontSize = 13.sp, color = textColor, modifier = Modifier.weight(1f))
                                                    if (isStudentChoice) {
                                                        Icon(
                                                            imageVector = if (isCorrectAnswer) Icons.Default.CheckCircle else Icons.Default.Close,
                                                            contentDescription = null,
                                                            tint = textColor,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    } else if (isCorrectAnswer) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = textColor.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text("Jawaban Siswa:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(studentAns ?: "(Tidak dijawab)", fontSize = 14.sp, color = Color(0xFF1F2937))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                        // Tugas Rendering
                        item {
                            val context = LocalContext.current
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Teks Jawaban:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = submission.submissionText ?: "(Siswa tidak mengirimkan teks jawaban)",
                                        fontSize = 14.sp,
                                        color = if (submission.submissionText.isNullOrBlank()) Color.Gray else Color(0xFF4B5563)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text("File Lampiran:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (!submission.filePath.isNullOrBlank()) {
                                        Button(
                                            onClick = {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                                intent.data = android.net.Uri.parse(submission.filePath)
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E))
                                        ) {
                                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Buka Lampiran")
                                        }
                                    } else {
                                        Text("(Tidak ada lampiran file)", fontSize = 14.sp, color = Color.Gray)
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

@Composable
fun DiscussionSection(
    comments: List<com.pab.digitallearning.data.model.CommentItem>,
    isLoading: Boolean,
    onSendComment: (String, android.net.Uri?) -> Unit,
    onEditComment: (Int, String) -> Unit,
    onDeleteComment: (Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var commentToEdit by remember { mutableStateOf<com.pab.digitallearning.data.model.CommentItem?>(null) }
    var editText by remember { mutableStateOf("") }
    var menuExpandedFor by remember { mutableStateOf<Int?>(null) }
    var showImageChooser by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var expandedImageUri by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
        }
    }

    fun launchCamera() {
        val file = java.io.File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    if (showImageChooser) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showImageChooser = false },
            title = { Text("Pilih Gambar") },
            text = { Text("Dari mana Anda ingin mengambil gambar?") },
            confirmButton = {
                TextButton(onClick = {
                    showImageChooser = false
                    launchCamera()
                }) { Text("Kamera") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageChooser = false
                    galleryLauncher.launch("image/*")
                }) { Text("Galeri") }
            }
        )
    }

    if (expandedImageUri != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { expandedImageUri = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                coil.compose.AsyncImage(
                    model = expandedImageUri,
                    contentDescription = "Gambar Penuh",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                IconButton(
                    onClick = { expandedImageUri = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha=0.5f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Tutup", tint = Color.White)
                }
            }
        }
    }

    if (commentToEdit != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { commentToEdit = null },
            title = { Text("Edit Komentar") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEditComment(commentToEdit!!.id, editText)
                    commentToEdit = null
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { commentToEdit = null }) { Text("Batal") }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        Text("Forum Diskusi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF102B5E))
        Spacer(modifier = Modifier.height(12.dp))
        
        if (comments.isEmpty()) {
            Text("Belum ada komentar. Jadilah yang pertama bertanya!", color = Color.Gray, fontSize = 14.sp)
        } else {
            comments.forEach { comment ->
                val isTeacher = comment.user.role == "guru"
                val bgColor = if (isTeacher) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                val nameColor = if (isTeacher) Color(0xFF1976D2) else Color.Black
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!comment.user.fotoProfile.isNullOrEmpty()) {
                                coil.compose.AsyncImage(
                                    model = comment.user.fotoProfile,
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.LightGray, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = (comment.user.namaLengkap ?: comment.user.nama) + if (isTeacher) " (Guru)" else "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = nameColor
                                )
                                val dateString = try {
                                    val parsed = java.time.Instant.parse(comment.createdAt)
                                    val local = java.time.LocalDateTime.ofInstant(parsed, java.time.ZoneId.systemDefault())
                                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
                                    local.format(formatter)
                                } catch (e: Exception) {
                                    comment.createdAt
                                }
                                Text(text = dateString, fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        if (comment.isMine) {
                            Box {
                                IconButton(
                                    onClick = { menuExpandedFor = comment.id },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Opsi",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.Gray
                                    )
                                }
                                androidx.compose.material3.DropdownMenu(
                                    expanded = menuExpandedFor == comment.id,
                                    onDismissRequest = { menuExpandedFor = null }
                                ) {
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = {
                                            menuExpandedFor = null
                                            commentToEdit = comment
                                            editText = comment.komentar
                                        }
                                    )
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Hapus") },
                                        onClick = {
                                            menuExpandedFor = null
                                            onDeleteComment(comment.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = comment.komentar, fontSize = 14.sp)
                    
                    if (!comment.imagePath.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        coil.compose.AsyncImage(
                            model = comment.imagePath,
                            contentDescription = "Lampiran Gambar",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.05f))
                                .clickable { expandedImageUri = comment.imagePath },
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedImageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gambar terpilih", color = Color(0xFF4CAF50), fontSize = 12.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = { selectedImageUri = null }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Batal", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showImageChooser = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Add,
                    contentDescription = "Lampirkan Gambar",
                    tint = Color(0xFF102B5E)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tulis balasan...", fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF102B5E),
                    focusedLabelColor = Color(0xFF102B5E)
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { 
                    if (text.isNotBlank() || selectedImageUri != null) {
                        onSendComment(text, selectedImageUri)
                        text = ""
                        selectedImageUri = null
                    }
                },
                enabled = !isLoading && (text.isNotBlank() || selectedImageUri != null),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Kirim")
                }
            }
        }
    }
}
