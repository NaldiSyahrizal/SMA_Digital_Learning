package com.pab.digitallearning.ui.student.content

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.digitallearning.data.model.StudentAttemptSubmission
import com.pab.digitallearning.data.model.StudentContentDetailData
import com.pab.digitallearning.util.DateTimeUtils



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentContentDetailScreen(
    contentId: Long,
    judul: String,
    viewModel: StudentContentViewModel,
    token: String?,
    onBackClick: () -> Unit,
    onPlayQuizClick: (Long, Int) -> Unit, // passes contentId and durationMinutes
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.detailState.collectAsState()
    val submitTaskState by viewModel.submitTaskState.collectAsState()
    val commentsState by viewModel.commentsState.collectAsState()
    val postCommentLoading by viewModel.postCommentLoading.collectAsState()
    val context = LocalContext.current

    var submissionText by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var clearFile by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = contentId, key2 = token) {
        viewModel.fetchContentDetail(token, contentId)
        viewModel.fetchComments(token ?: "", contentId)
        viewModel.resetSubmitStates()
    }

    // Process Task submit state
    LaunchedEffect(key1 = submitTaskState) {
        when (val state = submitTaskState) {
            is SubmitTaskUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                submissionText = ""
                selectedFileUri = null
                selectedFileName = ""
                clearFile = false
                viewModel.resetSubmitStates()
            }
            is SubmitTaskUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetSubmitStates()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = detailState) {
                is StudentContentDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF102B5E))
                    }
                }
                is StudentContentDetailUiState.Success -> {
                    val data = state.data
                    val countdown = DateTimeUtils.getCountdownString(data.dueDate)
                    val isStrictlyClosed = DateTimeUtils.isStrictlyClosed(data.isClosed)
                    val isOverdue = DateTimeUtils.isOverdue(data.dueDate)
                    // isClosedAndOverdue kept for legacy components that need full block
                    val isClosedAndOverdue = isStrictlyClosed

                    // Pick File Launcher
                    val mimeTypeFilter = when (data.allowedFileTypes?.lowercase()) {
                        "pdf" -> "application/pdf"
                        "image" -> "image/*"
                        else -> "*/*"
                    }

                    val filePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let {
                            selectedFileUri = it
                            clearFile = false
                            // Try to read file name
                            val cursor = context.contentResolver.query(it, null, null, null, null)
                            if (cursor != null && cursor.moveToFirst()) {
                                val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                if (nameIdx != -1) {
                                    selectedFileName = cursor.getString(nameIdx)
                                }
                                cursor.close()
                            }
                            if (selectedFileName.isEmpty()) {
                                selectedFileName = it.lastPathSegment ?: "berkas_tugas"
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Content Card (Title + Description + Attachment download link)
                        item {
                            ContentMainCard(
                                data = data,
                                countdown = countdown,
                                isClosedAndOverdue = isClosedAndOverdue,
                                onDownloadClick = { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            )
                        }

                        // 2. Play Quiz action OR Submit task action
                        if (data.tipe.lowercase() == "kuis") {
                            item {
                                QuizStartCard(
                                    data = data,
                                    isStrictlyClosed = isStrictlyClosed,
                                    isOverdue = isOverdue,
                                    onPlayClick = {
                                        onPlayQuizClick(data.id, data.quizDurationMinutes ?: 30)
                                    }
                                )
                            }
                        } else if (data.tipe.lowercase() == "tugas") {
                            val isGraded = data.submissions.any { it.status.lowercase() == "graded" }
                            item {
                                TaskSubmitCard(
                                    data = data,
                                    isStrictlyClosed = isStrictlyClosed,
                                    isOverdue = isOverdue,
                                    isGraded = isGraded,
                                    submissionText = submissionText,
                                    selectedFileName = selectedFileName,
                                    onTextChange = { submissionText = it },
                                    onPickFile = { filePickerLauncher.launch(mimeTypeFilter) },
                                    onClearFile = {
                                        selectedFileUri = null
                                        selectedFileName = ""
                                        clearFile = true
                                    },
                                    onSubmit = {
                                        viewModel.submitTask(token, data.id, submissionText, selectedFileUri, clearFile, context)
                                    },
                                    isLoading = submitTaskState is SubmitTaskUiState.Loading
                                )
                            }
                        }

                        // 3. Submissions History Section
                        if (data.submissions.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Tugas Yang dikumpulkan & nilai",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF102B5E)
                                )
                            }

                            items(data.submissions) { sub ->
                                SubmissionHistoryCard(
                                    submission = sub,
                                    dueDate = data.dueDate,
                                    onDownloadFile = { url ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                    onEditClick = {
                                        submissionText = sub.submissionText ?: ""
                                        selectedFileUri = null
                                        selectedFileName = sub.filePath?.substringAfterLast('/') ?: ""
                                        clearFile = false
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteSubmission(token, sub.id, data.id)
                                    }
                                )
                            }
                        }

                        item {
                            DiscussionSection(
                                comments = commentsState,
                                isLoading = postCommentLoading,
                                onSendComment = { text, uri ->
                                    viewModel.postComment(token ?: "", contentId, text, uri, context)
                                },
                                onEditComment = { commentId, newText ->
                                    viewModel.editComment(token ?: "", contentId, commentId, newText) { success, msg ->
                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDeleteComment = { commentId ->
                                    viewModel.deleteComment(token ?: "", contentId, commentId) { success, msg ->
                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
                is StudentContentDetailUiState.Error -> {
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
                            onClick = { viewModel.fetchContentDetail(token, contentId) },
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

@Composable
fun ContentMainCard(
    data: StudentContentDetailData,
    countdown: String,
    isClosedAndOverdue: Boolean,
    onDownloadClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Label category
            Box(
                modifier = Modifier
                    .background(Color(0xFFEBF3FC), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = data.tipe.uppercase(),
                    color = Color(0xFF007AFF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = data.judul,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF102B5E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = data.deskripsi,
                fontSize = 13.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )

            // Show Deadline if not a basic materi
            if (data.tipe.lowercase() != "materi" && !data.dueDate.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF2F4F7))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tenggat Pengumpulan:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Text(
                        text = countdown,
                        color = if (isClosedAndOverdue) Color(0xFFFF3B30) else Color(0xFF34C759),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Download attachment link if present
            if (!data.filePath.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { onDownloadClick(data.filePath) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF102B5E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unduh Lampiran Materi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuizStartCard(
    data: StudentContentDetailData,
    isStrictlyClosed: Boolean,
    isOverdue: Boolean,
    onPlayClick: () -> Unit
) {
    val attemptsCount = data.submissions.size
    val max = data.quizMaxAttempts ?: 0
    val isAttemptsExhausted = max > 0 && attemptsCount >= max

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Evaluasi Kuis Akademik",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF102B5E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quiz specs info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${data.questions.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF102B5E))
                    Text(text = "Pertanyaan", fontSize = 10.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${data.quizDurationMinutes ?: 30}m", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF102B5E))
                    Text(text = "Durasi", fontSize = 10.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (max > 0) "$attemptsCount/$max" else "$attemptsCount (Bebas)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF102B5E)
                    )
                    Text(text = "Percobaan", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isStrictlyClosed -> {
                    // Teacher explicitly closed – full block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFF3B30).copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Kuis telah ditutup oleh guru dan tidak dapat dikerjakan.",
                            color = Color(0xFFFF3B30),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                isAttemptsExhausted -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFF9500).copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Batas Percobaan Habis. Anda telah mengerjakan kuis sebanyak $max kali.",
                            color = Color(0xFFFF9500),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    // Overdue warning – but can still play
                    if (isOverdue) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFF9500).copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "⚠️ Tenggat waktu telah terlewati. Pengerjaan ini akan dicatat sebagai TERLAMBAT.",
                                color = Color(0xFFFF9500),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOverdue) Color(0xFFFF9500) else Color(0xFF34C759)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (attemptsCount > 0) "Mulai Percobaan Baru" else "Mulai Ujian Kuis",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskSubmitCard(
    data: StudentContentDetailData,
    isStrictlyClosed: Boolean,
    isOverdue: Boolean,
    isGraded: Boolean,
    submissionText: String,
    selectedFileName: String,
    onTextChange: (String) -> Unit,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    val fileLabel = when (data.allowedFileTypes?.lowercase()) {
        "pdf" -> "Hanya berkas format PDF yang diizinkan"
        "image" -> "Hanya berkas format Gambar (PNG/JPG) yang diizinkan"
        else -> "Format berkas bebas (PDF atau Gambar)"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Lembar Pengumpulan Tugas",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF102B5E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isStrictlyClosed) {
                // Teacher explicitly closed – no submission allowed
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFF3B30).copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pengumpulan ditutup oleh guru. Tugas ini tidak dapat dikumpulkan.",
                        color = Color(0xFFFF3B30),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (isGraded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF34C759).copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tugas ini telah dinilai oleh guru dan tidak dapat dikumpulkan kembali.",
                        color = Color(0xFF34C759),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Overdue warning – submission still allowed (late)
                if (isOverdue) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFF9500).copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "⚠️ Tenggat waktu telah terlewati. Pengumpulan ini akan dicatat sebagai TERLAMBAT.",
                            color = Color(0xFFFF9500),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Submission Text area
                OutlinedTextField(
                    value = submissionText,
                    onValueChange = onTextChange,
                    placeholder = { Text("Tulis jawaban atau catatan tugas di sini (opsional)...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // File Upload module
                Text(
                    text = "Unggah Lampiran:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Text(
                    text = fileLabel,
                    fontSize = 10.sp,
                    color = Color(0xFF102B5E),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPickFile,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEBF3FC)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Pilih Berkas", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (selectedFileName.isNotEmpty()) selectedFileName else "Belum ada berkas dipilih",
                        fontSize = 12.sp,
                        color = if (selectedFileName.isNotEmpty()) Color.DarkGray else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (selectedFileName.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onClearFile,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Berkas",
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF102B5E), modifier = Modifier.size(24.dp))
                    }
                } else {
                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOverdue) Color(0xFFFF9500) else Color(0xFF102B5E)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (data.submissions.isNotEmpty()) "Kirim Ulang Jawaban" else "Kumpul Sekarang",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionHistoryCard(
    submission: StudentAttemptSubmission,
    dueDate: String?,
    onDownloadFile: (String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Compute late info badge
    val lateInfo = DateTimeUtils.getLateInfo(submission.updatedAt, dueDate)
    val isLate = lateInfo != null && lateInfo.startsWith("Terlambat")

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Date/Time (Percobaan Ke-n removed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Date + Time
                val dateStr = submission.updatedAt.substringBefore("T")
                val timeStr = submission.updatedAt.substringAfter("T").take(5)
                Text(
                    text = "$dateStr $timeStr",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            // Late / On-time badge
            if (lateInfo != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = if (isLate) Color(0xFFFF9500).copy(alpha = 0.12f) else Color(0xFF34C759).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isLate) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isLate) Color(0xFFFF9500) else Color(0xFF34C759),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = lateInfo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLate) Color(0xFFFF9500) else Color(0xFF34C759)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student notes text if any
            if (!submission.submissionText.isNullOrBlank()) {
                Text(
                    text = submission.submissionText,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Attached file if any
            if (!submission.filePath.isNullOrBlank()) {
                Text(
                    text = "File Lampiran Terunggah:",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDownloadFile(submission.filePath) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lihat Berkas Terkirim",
                        color = Color(0xFF007AFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Grade / Notes from teacher if present
            if (submission.status.lowercase() == "graded") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEAF9EE), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Koreksi Guru:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34C759)
                            )
                            Text(
                                text = "Skor: ${submission.nilai}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF34C759)
                            )
                        }
                        if (!submission.catatan.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Catatan: ${submission.catatan}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F4F7), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Sedang menunggu pemeriksaan/penilaian oleh Guru.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onEditClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF007AFF)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ubah",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ubah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF3B30)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
