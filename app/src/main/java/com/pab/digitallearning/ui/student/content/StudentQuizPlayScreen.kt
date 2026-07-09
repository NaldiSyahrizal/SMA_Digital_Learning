package com.pab.digitallearning.ui.student.content

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import coil.compose.AsyncImage
import com.pab.digitallearning.data.model.StudentQuizQuestion
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentQuizPlayScreen(
    contentId: Long,
    durationMinutes: Int,
    viewModel: StudentContentViewModel,
    token: String?,
    onBackClick: () -> Unit,
    onQuizFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.detailState.collectAsState()
    val submitQuizState by viewModel.submitQuizState.collectAsState()
    val context = LocalContext.current

    val sharedPrefs = remember { context.getSharedPreferences("quiz_prefs", android.content.Context.MODE_PRIVATE) }
    val startTimeKey = "quiz_start_${contentId}"
    val answersKey = "quiz_answers_${contentId}"
    val exitCountKey = "quiz_exit_count_${contentId}"
    val exitLogsKey = "quiz_exit_logs_${contentId}"
    val exitStartKey = "quiz_exit_start_${contentId}"

    // Ensure state clean up if fresh start
    val isFreshStart = remember { sharedPrefs.getLong(startTimeKey, 0L) == 0L }
    if (isFreshStart) {
        val now = System.currentTimeMillis()
        sharedPrefs.edit()
            .putLong(startTimeKey, now)
            .putInt(exitCountKey, 0)
            .putString(exitLogsKey, "[]")
            .putLong(exitStartKey, 0L)
            .apply()
    }

    var activeQuestionIndex by remember { mutableStateOf(0) }
    
    // Load initial saved answers from shared preferences
    val initialAnswers = remember {
        val map = mutableMapOf<Long, String>()
        val savedAnswersJson = sharedPrefs.getString(answersKey, null)
        if (!savedAnswersJson.isNullOrEmpty()) {
            try {
                val parsed = com.google.gson.Gson().fromJson<Map<String, String>>(
                    savedAnswersJson,
                    object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                )
                parsed.forEach { (k, v) -> map[k.toLong()] = v }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        map
    }
    val answersMap = remember { mutableStateMapOf<Long, String>().apply { putAll(initialAnswers) } }

    var showSubmitConfirmation by remember { mutableStateOf(false) }
    var showExitOptionsDialog by remember { mutableStateOf(false) }
    var showResumeWarning by remember { mutableStateOf(false) }
    
    var secondsRemaining by remember {
        val savedStartVal = sharedPrefs.getLong(startTimeKey, 0L)
        val elapsed = (System.currentTimeMillis() - savedStartVal) / 1000
        val remaining = (durationMinutes * 60) - elapsed
        mutableStateOf(remaining.coerceAtLeast(0L).toInt())
    }

    // States for tracking exits and cheating logs (loaded from SharedPreferences for persistence)
    var exitCount by remember {
        mutableStateOf(sharedPrefs.getInt(exitCountKey, 0))
    }

    val exitLogs = remember {
        val list = mutableStateListOf<String>()
        val savedLogsJson = sharedPrefs.getString(exitLogsKey, null)
        if (!savedLogsJson.isNullOrEmpty()) {
            try {
                val parsed = com.google.gson.Gson().fromJson<List<String>>(
                    savedLogsJson,
                    object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                )
                list.addAll(parsed)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        list
    }

    // Helper functions to log exit and update preferences
    val logExit = { startTime: Long, endTime: Long ->
        val duration = (endTime - startTime) / 1000
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        val startTimeStr = sdf.format(Date(startTime))
        val endTimeStr = sdf.format(Date(endTime))
        val log = "Keluar $exitCount: $startTimeStr s.d. $endTimeStr ($duration detik)"
        
        exitLogs.add(log)
        sharedPrefs.edit()
            .putString(exitLogsKey, com.google.gson.Gson().toJson(exitLogs.toList()))
            .apply()
    }

    // State for tracking flagged questions (Ragu-ragu)
    val flaggedQuestions = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { mutableStateListOf<Long>().apply { addAll(it as List<Long>) } }
        )
    ) {
        mutableStateListOf<Long>()
    }

    // State for Zoomable Image
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

    // Intercept hardware or gesture back navigation to show exit options
    BackHandler(enabled = true) {
        showExitOptionsDialog = true
    }

    // Observe lifecycle events to detect exits
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    val exitStart = sharedPrefs.getLong(exitStartKey, 0L)
                    if (exitStart == 0L) {
                        val now = System.currentTimeMillis()
                        val newCount = exitCount + 1
                        exitCount = newCount
                        sharedPrefs.edit()
                            .putLong(exitStartKey, now)
                            .putInt(exitCountKey, newCount)
                            .apply()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    val exitStart = sharedPrefs.getLong(exitStartKey, 0L)
                    if (exitStart != 0L) {
                        val now = System.currentTimeMillis()
                        logExit(exitStart, now)
                        sharedPrefs.edit().putLong(exitStartKey, 0L).apply()
                        showResumeWarning = true // Show warning dialog on app resume
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = contentId, key2 = token) {
        viewModel.fetchContentDetail(token, contentId)
        viewModel.resetSubmitStates()
        
        // Initial entry warning toast
        if (isFreshStart) {
            Toast.makeText(context, "⚠️ Peringatan: Jangan keluar dari kuis atau aplikasi selama pengerjaan. Seluruh tindakan keluar akan dicatat!", Toast.LENGTH_LONG).show()
        }

        // Fast-check if returning to screen from a page exit
        val exitStart = sharedPrefs.getLong(exitStartKey, 0L)
        if (exitStart != 0L) {
            val now = System.currentTimeMillis()
            logExit(exitStart, now)
            sharedPrefs.edit().putLong(exitStartKey, 0L).apply()
            showResumeWarning = true
        }
    }

    // Active countdown timer ticking down
    LaunchedEffect(key1 = secondsRemaining) {
        if (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
        } else {
            // Automatically submit answers when timer expires!
            viewModel.submitQuiz(token, contentId, answersMap.toMap(), exitCount, exitLogs.toList())
            Toast.makeText(context, "Waktu Ujian telah Habis! Jawaban Anda otomatis terkirim.", Toast.LENGTH_LONG).show()
        }
    }

    // Process Quiz submission states
    LaunchedEffect(key1 = submitQuizState) {
        when (val state = submitQuizState) {
            is SubmitQuizUiState.Success -> {
                sharedPrefs.edit()
                    .remove(startTimeKey)
                    .remove(answersKey)
                    .remove(exitCountKey)
                    .remove(exitLogsKey)
                    .remove(exitStartKey)
                    .apply()
                Toast.makeText(context, "Kuis Selesai! Skor: ${state.response.nilai}", Toast.LENGTH_LONG).show()
                onQuizFinished()
                viewModel.resetSubmitStates()
            }
            is SubmitQuizUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetSubmitStates()
            }
            else -> {}
        }
    }

    // Helper format MM:SS
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeString = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Lembar Ujian Kuis",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Sisa Waktu: $timeString",
                            color = if (secondsRemaining < 60) Color(0xFFFF3B30) else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Confirm before exiting active quiz
                        showExitOptionsDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Keluar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF102B5E)
                )
            )
        },
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
                    val questions = state.data.questions

                    if (questions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada pertanyaan tersedia untuk kuis ini.", color = Color.Gray)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Horizontal Question Grid navigation bar
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(questions) { index, q ->
                                    val isSelected = activeQuestionIndex == index
                                    val isAnswered = answersMap.containsKey(q.id)
                                    val isFlagged = flaggedQuestions.contains(q.id)

                                    val containerColor = when {
                                        isSelected -> Color(0xFF102B5E)
                                        isFlagged -> Color(0xFFFFF3E0)
                                        isAnswered -> Color(0xFFEAF9EE)
                                        else -> Color.White
                                    }
                                    val contentColor = when {
                                        isSelected -> Color.White
                                        isFlagged -> Color(0xFFFF9500)
                                        isAnswered -> Color(0xFF34C759)
                                        else -> Color(0xFF102B5E)
                                    }
                                    val border = when {
                                        isSelected && isFlagged -> BorderStroke(2.dp, Color(0xFFFF9500))
                                        isSelected -> null
                                        isFlagged -> BorderStroke(1.dp, Color(0xFFFF9500).copy(alpha = 0.5f))
                                        else -> BorderStroke(1.dp, Color(0xFF102B5E).copy(alpha = 0.2f))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = containerColor,
                                        border = border,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clickable { activeQuestionIndex = index }
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
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

                            // 2. Active Question Card
                            val q = questions[activeQuestionIndex]
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp)
                                ) {
                                    // Question Header
                                    Text(
                                        text = "Pertanyaan #${activeQuestionIndex + 1} dari ${questions.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Question Text
                                    Text(
                                        text = q.pertanyaan,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF102B5E),
                                        lineHeight = 20.sp
                                    )

                                    // Question Image supporting if present
                                    if (!q.imagePath.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFF2F4F7))
                                                .clickable {
                                                    zoomImageUrl = q.imagePath
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = q.imagePath,
                                                contentDescription = "Gambar Pendukung Soal (Ketuk untuk memperbesar)",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Ragu-ragu (Tandai Soal)
                                    val isFlagged = flaggedQuestions.contains(q.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isFlagged) Color(0xFFFFF3E0) else Color(0xFFF8FAFD),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                if (isFlagged) {
                                                    flaggedQuestions.remove(q.id)
                                                } else {
                                                    flaggedQuestions.add(q.id)
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isFlagged,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    flaggedQuestions.add(q.id)
                                                } else {
                                                    flaggedQuestions.remove(q.id)
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFFFF9500),
                                                uncheckedColor = Color.Gray
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ragu-ragu (Tandai Soal)",
                                            color = if (isFlagged) Color(0xFFFF9500) else Color.DarkGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (q.tipeSoal == "essay") {
                                        OutlinedTextField(
                                            value = answersMap[q.id] ?: "",
                                            onValueChange = { newValue ->
                                                answersMap[q.id] = newValue
                                                val json = com.google.gson.Gson().toJson(answersMap.toMap())
                                                sharedPrefs.edit().putString(answersKey, json).apply()
                                            },
                                            label = { Text("Jawaban Esai") },
                                            placeholder = { Text("Ketik jawaban Anda di sini...") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    } else {
                                        // Render Options (A, B, C, D)
                                        val options = listOf(
                                            "A" to (q.opsiA ?: ""),
                                            "B" to (q.opsiB ?: ""),
                                            "C" to (q.opsiC ?: ""),
                                            "D" to (q.opsiD ?: "")
                                        )

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            options.forEach { (optionKey, optionText) ->
                                                if (optionText.isNotBlank()) {
                                                    val isSelected = answersMap[q.id] == optionKey
                                                    val optionBg = if (isSelected) Color(0xFF102B5E).copy(alpha = 0.08f) else Color.White
                                                    val optionBorder = if (isSelected) Color(0xFF102B5E) else Color.LightGray.copy(alpha = 0.5f)
                                                    val optionTextColor = if (isSelected) Color(0xFF102B5E) else Color.DarkGray

                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = optionBg,
                                                        border = BorderStroke(1.dp, optionBorder),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                answersMap[q.id] = optionKey
                                                                val json = com.google.gson.Gson().toJson(answersMap.toMap())
                                                                sharedPrefs.edit().putString(answersKey, json).apply()
                                                            }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(24.dp)
                                                                    .background(
                                                                        if (isSelected) Color(0xFF102B5E) else Color(0xFFF2F4F7),
                                                                        RoundedCornerShape(6.dp)
                                                                    ),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = optionKey,
                                                                    color = if (isSelected) Color.White else Color.Gray,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Black
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.width(12.dp))

                                                            Text(
                                                                text = optionText,
                                                                color = optionTextColor,
                                                                fontSize = 13.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }

                            // 3. Bottom Action Navigation Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Previous Button
                                OutlinedButton(
                                    onClick = {
                                        if (activeQuestionIndex > 0) activeQuestionIndex--
                                    },
                                    enabled = activeQuestionIndex > 0,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Sebelumnya", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                // Submit or Next Button
                                val isLastQuestion = activeQuestionIndex == questions.size - 1
                                if (isLastQuestion) {
                                    Button(
                                        onClick = { showSubmitConfirmation = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Kirim Ujian", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (activeQuestionIndex < questions.size - 1) activeQuestionIndex++
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Selanjutnya", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
                is StudentContentDetailUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message, color = Color.Red)
                    }
                }
            }

            // Submit Confirmation Dialog (When clicking Kirim Ujian at the last question)
            if (showSubmitConfirmation) {
                val answeredCount = answersMap.size
                val totalCount = (detailState as? StudentContentDetailUiState.Success)?.data?.questions?.size ?: 0

                AlertDialog(
                    onDismissRequest = { showSubmitConfirmation = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White,
                    title = {
                        Text(
                            text = "Kirim Jawaban Ujian?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF102B5E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "Anda telah menjawab $answeredCount dari $totalCount pertanyaan. Apakah Anda yakin ingin mengakhiri dan mengirimkan lembar ujian kuis ini?",
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSubmitConfirmation = false
                                viewModel.submitQuiz(token, contentId, answersMap.toMap(), exitCount, exitLogs.toList())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ya, Kirim", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSubmitConfirmation = false }) {
                            Text("Periksa Lagi", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            // Exit Options Dialog (When clicking Back button or gesture)
            if (showExitOptionsDialog) {
                val answeredCount = answersMap.size
                val totalCount = (detailState as? StudentContentDetailUiState.Success)?.data?.questions?.size ?: 0

                AlertDialog(
                    onDismissRequest = { showExitOptionsDialog = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White,
                    title = {
                        Text(
                            text = "Keluar dari Kuis?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF102B5E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Anda telah menjawab $answeredCount dari $totalCount pertanyaan.",
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Pilih 'Kirim Ujian' untuk menyelesaikan kuis sekarang, atau 'Keluar Sementara' jika ingin kembali ke halaman sebelumnya. Catatan: Jika keluar sementara, sisa waktu akan terus berjalan dan akan dicatat sebagai keluar kuis.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Option 1: Submit and Exit
                            Button(
                                onClick = {
                                    showExitOptionsDialog = false
                                    viewModel.submitQuiz(token, contentId, answersMap.toMap(), exitCount, exitLogs.toList())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Kirim Ujian Sekarang", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            
                            // Option 2: Exit Temporarily
                            OutlinedButton(
                                onClick = {
                                    showExitOptionsDialog = false
                                    // Record the exit start time
                                    val now = System.currentTimeMillis()
                                    val newCount = exitCount + 1
                                    exitCount = newCount
                                    sharedPrefs.edit()
                                        .putLong(exitStartKey, now)
                                        .putInt(exitCountKey, newCount)
                                        .apply()
                                    onBackClick() // Navigate back
                                },
                                border = BorderStroke(1.dp, Color(0xFFFF9500)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Keluar Sementara (Catat Keluar)", color = Color(0xFFFF9500), fontWeight = FontWeight.Bold)
                            }

                            // Option 3: Cancel
                            TextButton(
                                onClick = { showExitOptionsDialog = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Batal & Lanjutkan Kuis", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                )
            }

            // Resume Warning Dialog
            if (showResumeWarning) {
                AlertDialog(
                    onDismissRequest = { /* Prevent dismiss by clicking outside */ },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peringatan",
                            tint = Color(0xFFFF9500),
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Peringatan Pelanggaran!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFFF9500),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Anda terdeteksi keluar dari halaman kuis atau aplikasi selama pengerjaan kuis berlangsung.",
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Total keluar: $exitCount kali",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF3B30),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Waktu kuis tetap berjalan dan catatan aktivitas ini telah terekam di sistem untuk diperiksa oleh guru.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showResumeWarning = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Saya Mengerti & Lanjutkan", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Zoomable Image Viewer Dialog
            zoomImageUrl?.let { imageUrl ->
                ZoomableImageDialog(
                    imagePath = imageUrl,
                    onDismiss = { zoomImageUrl = null }
                )
            }
        }
    }
}

@Composable
fun ZoomableImageDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset += pan
                            } else {
                                offset = androidx.compose.ui.geometry.Offset.Zero
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = "Gambar Soal Diperbesar",
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = Color.White
                )
            }
        }
    }
}
