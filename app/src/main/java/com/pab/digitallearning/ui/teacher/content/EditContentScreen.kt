package com.pab.digitallearning.ui.teacher.content

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.pab.digitallearning.core.SessionManager
import com.pab.digitallearning.data.model.QuizQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContentScreen(
    viewModel: TeacherContentViewModel,
    contentId: Long,
    classId: Long,
    className: String,
    subjectId: Long,
    subjectName: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    val contentDetailState by viewModel.contentDetailState.collectAsState()
    
    var isInitialized by remember { mutableStateOf(false) }
    
    var tipe by remember { mutableStateOf("materi") }
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var isClosed by remember { mutableStateOf(false) }
    var closeAutomatically by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempSelectedDate by remember { mutableStateOf("") }
    
    // SAW and Quiz attributes
    var weight by remember { mutableStateOf("") }   // stored as String for input
    var quizDurationMinutes by remember { mutableStateOf("") }
    var quizMaxAttempts by remember { mutableStateOf("1") }
    var allowedFileTypes by remember { mutableStateOf("all") }
    
    // Attachment File Picker State
    var fileName by remember { mutableStateOf<String?>(null) }
    var fileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var existingFilePath by remember { mutableStateOf<String?>(null) }

    // Quiz Questions List State
    val questions = remember { mutableStateListOf<QuizQuestion>() }
    
    var isSaving by remember { mutableStateOf(false) }

    val classesState by viewModel.classesState.collectAsState()
    val selectedClassIds = remember { mutableStateListOf<Long>() }

    // Quiz Questions Supporting Image Selection State
    var activeQuestionImageIndex by remember { mutableStateOf<Int?>(null) }
    var activeQuestionIndex by remember { mutableStateOf(0) }
    
    val questionImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val index = activeQuestionImageIndex
        if (uri != null && index != null && index in questions.indices) {
            val q = questions[index]
            var qFileName = "gambar_pertanyaan.jpg"
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) qFileName = c.getString(nameIndex)
                }
            }
            val qBytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes()
            }
            questions[index] = q.copy(
                localImageUri = uri.toString(),
                localImageName = qFileName,
                localImageBytes = qBytes
            )
        }
        activeQuestionImageIndex = null
    }

    LaunchedEffect(contentId) {
        viewModel.fetchContentDetail(sessionManager.getToken(), contentId)
        viewModel.fetchClasses(sessionManager.getToken())
    }

    LaunchedEffect(contentDetailState) {
        val state = contentDetailState
        if (state is ContentDetailUiState.Success && !isInitialized) {
            val content = state.content
            tipe = content.tipe
            judul = content.judul
            deskripsi = content.deskripsi
            dueDate = if (content.dueDate != null) {
                // Formatting YYYY-MM-DD HH:MM from ISO string
                val datePart = content.dueDate.substringBefore("T")
                val timePart = content.dueDate.substringAfter("T").take(5)
                "$datePart $timePart"
            } else ""
            
            existingFilePath = content.filePath
            if (!content.filePath.isNullOrBlank()) {
                fileName = content.filePath.substringAfterLast("/")
            }
            
            weight = (content.weight ?: "").toString().let { if (it == "0") "" else it }
            quizDurationMinutes = content.quizDurationMinutes?.toString() ?: ""
            quizMaxAttempts = content.quizMaxAttempts?.toString() ?: "1"
            allowedFileTypes = content.allowedFileTypes ?: "all"
            isClosed = content.isClosed
            closeAutomatically = content.closeAutomatically
            
            questions.clear()
            questions.addAll(content.questions)
            
            selectedClassIds.clear()
            content.activeGroupClassIds?.let { selectedClassIds.addAll(it) }
            if (!selectedClassIds.contains(classId)) {
                selectedClassIds.add(classId)
            }
            
            isInitialized = true
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) fileName = c.getString(nameIndex)
                }
            }
            if (fileName == null) fileName = "lampiran_file"
            
            context.contentResolver.openInputStream(it)?.use { stream ->
                fileBytes = stream.readBytes()
            }
            existingFilePath = null // Defer to newly picked file bytes
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFD),
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            if (judul.isBlank()) {
                                Toast.makeText(context, "Judul konten wajib diisi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (deskripsi.isBlank()) {
                                Toast.makeText(context, "Deskripsi konten wajib diisi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (tipe != "materi" && dueDate.isBlank()) {
                                Toast.makeText(context, "Tenggat waktu wajib diisi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                             if (tipe != "materi" && weight.isBlank()) {
                                 Toast.makeText(context, "Bobot tugas wajib diisi!", Toast.LENGTH_SHORT).show()
                                 return@Button
                             }
                            if (tipe == "kuis" && questions.isEmpty()) {
                                Toast.makeText(context, "Kuis wajib memiliki minimal 1 soal!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                             isSaving = true
                             viewModel.updateContent(
                                 token = sessionManager.getToken(),
                                 contentId = contentId,
                                 judul = judul,
                                 deskripsi = deskripsi,
                                 dueDate = if (dueDate.isBlank()) null else dueDate,
                                 isClosed = if (closeAutomatically) false else isClosed,
                                 closeAutomatically = closeAutomatically,
                                 questions = if (questions.isNotEmpty()) questions.toList() else null,
                                 weight = weight.toIntOrNull() ?: 30,
                                 quizDurationMinutes = if (tipe == "kuis" && quizDurationMinutes.isNotBlank()) quizDurationMinutes.toIntOrNull() else null,
                                 quizMaxAttempts = if (tipe == "kuis" && quizMaxAttempts.isNotBlank()) quizMaxAttempts.toIntOrNull() else null,
                                 allowedFileTypes = if (tipe == "tugas") allowedFileTypes else "all",
                                 classIds = selectedClassIds.toList(),
                                 fileBytes = fileBytes,
                                 fileName = fileName,
                                 onResult = { success, msg ->
                                     isSaving = false
                                     Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                     if (success) {
                                         onBackPressed()
                                     }
                                 }
                             )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = !isSaving && isInitialized
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Simpan Perubahan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (!isInitialized) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF102B5E))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Visual Content Type Info Card (Locked for edit)
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
                            Text(
                                text = "Tipe Konten (Terkunci)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Gray
                            )
                            val tipeColor = when (tipe) {
                                "materi" -> Color(0xFF10B981)
                                "tugas" -> Color(0xFF3B82F6)
                                else -> Color(0xFFF59E0B)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = tipeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = tipe.replaceFirstChar { it.uppercase() },
                                    color = tipeColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    // Main Form Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Informasi Konten",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF102B5E)
                            )
                            
                            OutlinedTextField(
                                value = judul,
                                onValueChange = { judul = it },
                                label = { Text("Judul Konten") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = deskripsi,
                                onValueChange = { deskripsi = it },
                                label = { Text("Deskripsi / Petunjuk") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (tipe != "materi") {
                                val calendar = java.util.Calendar.getInstance()
                            val datePickerDialog = remember {
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        tempSelectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                        showTimePicker = true
                                    },
                                    calendar.get(java.util.Calendar.YEAR),
                                    calendar.get(java.util.Calendar.MONTH),
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                )
                            }

                            if (showTimePicker) {
                                val timePickerState = rememberTimePickerState(
                                    initialHour = calendar.get(java.util.Calendar.HOUR_OF_DAY),
                                    initialMinute = calendar.get(java.util.Calendar.MINUTE),
                                    is24Hour = true
                                )
                                AlertDialog(
                                    onDismissRequest = { showTimePicker = false },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                val selectedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                                                dueDate = "$tempSelectedDate $selectedTime"
                                                showTimePicker = false
                                            }
                                        ) {
                                            Text("OK", color = Color(0xFF102B5E), fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showTimePicker = false }) {
                                            Text("Batal", color = Color.Gray)
                                        }
                                    },
                                    title = {
                                        Text(
                                            text = "Pilih Waktu (24 Jam)",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF102B5E)
                                        )
                                    },
                                    text = {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            TimeInput(
                                                state = timePickerState,
                                                colors = TimePickerDefaults.colors(
                                                    timeSelectorSelectedContainerColor = Color(0xFF102B5E).copy(alpha = 0.1f),
                                                    timeSelectorSelectedContentColor = Color(0xFF102B5E),
                                                    timeSelectorUnselectedContainerColor = Color(0xFFF3F4F6),
                                                    timeSelectorUnselectedContentColor = Color.Gray
                                                )
                                            )
                                        }
                                    },
                                    containerColor = Color.White,
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePickerDialog.show() }
                            ) {
                                OutlinedTextField(
                                    value = dueDate,
                                    onValueChange = {},
                                    label = { Text("Tenggat Waktu") },
                                    placeholder = { Text("Ketuk untuk memilih tanggal & waktu...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    readOnly = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { datePickerDialog.show() }) {
                                            Icon(Icons.Default.Info, contentDescription = null)
                                        }
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { closeAutomatically = !closeAutomatically }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tutup otomatis setelah tenggat",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF102B5E)
                                    )
                                    Text(
                                        text = "Siswa tidak dapat mengumpulkan setelah batas waktu berakhir",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Switch(
                                    checked = closeAutomatically,
                                    onCheckedChange = { closeAutomatically = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF102B5E)
                                    )
                                )
                            }

                            if (!closeAutomatically) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isClosed = !isClosed }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Tutup akses secara manual",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF102B5E)
                                        )
                                        Text(
                                            text = "Tutup akses pengerjaan bagi siswa saat ini juga",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Switch(
                                        checked = isClosed,
                                        onCheckedChange = { isClosed = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF102B5E)
                                        )
                                    )
                                }
                            }
                            }
                        }
                    }
                }

            if (tipe != "materi") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Pengaturan Bobot & Urgensi Tugas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF102B5E)
                            )
                            Text(
                                text = "Isi semua kolom di bawah ini untuk menentukan prioritas tugas. Semua field wajib diisi.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            
                            // Weight Input Field
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        val intVal = newValue.toIntOrNull()
                                        if (newValue.isEmpty() || (intVal != null && intVal in 1..100)) {
                                            weight = newValue
                                        } else if (intVal != null && intVal > 100) {
                                            weight = "100"
                                        }
                                    }
                                },
                                label = { Text("Bobot Penilaian (1-100) *") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                isError = weight.isBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // If Task, Allowed File Types Dropdown
                            if (tipe == "tugas") {
                                var fileTypeExpanded by remember { mutableStateOf(false) }
                                val fileTypeLabel = when (allowedFileTypes) {
                                    "all" -> "Bebas (PDF & Gambar)"
                                    "pdf" -> "Hanya PDF"
                                    "image" -> "Hanya Gambar"
                                    else -> "Bebas"
                                }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = fileTypeLabel,
                                        onValueChange = {},
                                        label = { Text("Format File Yang Diizinkan") },
                                        readOnly = true,
                                        trailingIcon = {
                                            IconButton(onClick = { fileTypeExpanded = !fileTypeExpanded }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { fileTypeExpanded = true },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    DropdownMenu(
                                        expanded = fileTypeExpanded,
                                        onDismissRequest = { fileTypeExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                    ) {
                                        listOf("all", "pdf", "image").forEach { value ->
                                            val label = when (value) {
                                                "all" -> "Bebas (PDF & Gambar)"
                                                "pdf" -> "Hanya PDF"
                                                "image" -> "Hanya Gambar"
                                                else -> "Bebas"
                                            }
                                            DropdownMenuItem(
                                                text = { Text(label) },
                                                onClick = {
                                                    allowedFileTypes = value
                                                    fileTypeExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // If Quiz, Quiz Settings (Duration & Max Attempts)
                            if (tipe == "kuis") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = quizDurationMinutes,
                                        onValueChange = { quizDurationMinutes = it },
                                        label = { Text("Durasi (Menit)") },
                                        placeholder = { Text("e.g. 60") },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = quizMaxAttempts,
                                        onValueChange = { quizMaxAttempts = it },
                                        label = { Text("Percobaan") },
                                        placeholder = { Text("0 = Tanpa Batas") },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                    // Parallel Class Selection Card
                    val classes = (classesState as? ClassesUiState.Success)?.classes ?: emptyList()
                    val parallelClasses = remember(classes, classId, subjectId) {
                        classes.filter { it.subjectId == subjectId && it.classId != classId }
                    }

                    if (parallelClasses.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Bagikan ke Kelas Pararel (Opsional)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF102B5E)
                                )
                                Text(
                                    text = "Pilih kelas pararel lain yang juga akan menerima konten ini secara otomatis.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    parallelClasses.forEach { pClass ->
                                        val isChecked = selectedClassIds.contains(pClass.classId)
                                        FilterChip(
                                            selected = isChecked,
                                            onClick = {
                                                if (isChecked) {
                                                    if (pClass.classId != classId) {
                                                        selectedClassIds.remove(pClass.classId)
                                                    }
                                                } else {
                                                    selectedClassIds.add(pClass.classId)
                                                }
                                            },
                                            label = { Text(pClass.className) },
                                            leadingIcon = {
                                                if (isChecked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF102B5E).copy(alpha = 0.15f),
                                                selectedLabelColor = Color(0xFF102B5E),
                                                selectedLeadingIconColor = Color(0xFF102B5E)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (tipe != "kuis") {
                    item {
                        // Attachment Card for Materi & Tugas
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Lampiran File (Opsional)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF102B5E)
                                )
                                
                                if (fileName != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF102B5E))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(fileName!!, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        }
                                        IconButton(
                                            onClick = {
                                                fileName = null
                                                fileBytes = null
                                                existingFilePath = null
                                            }
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { fileLauncher.launch("*/*") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF102B5E).copy(alpha = 0.1f),
                                            contentColor = Color(0xFF102B5E)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Pilih Berkas Baru", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Quiz Section
                if (tipe == "kuis") {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kelola Soal Kuis (${questions.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF102B5E)
                            )
                            TextButton(
                                onClick = {
                                    questions.add(
                                        QuizQuestion(
                                            pertanyaan = "",
                                            opsiA = "",
                                            opsiB = "",
                                            opsiC = "",
                                            opsiD = "",
                                            jawabanBenar = "A"
                                        )
                                    )
                                    activeQuestionIndex = questions.size - 1
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF102B5E))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah Soal", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (questions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = {
                                            questions.add(
                                                QuizQuestion(
                                                    pertanyaan = "",
                                                    opsiA = "",
                                                    opsiB = "",
                                                    opsiC = "",
                                                    opsiD = "",
                                                    jawabanBenar = "A"
                                                )
                                            )
                                            activeQuestionIndex = 0
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tambah Soal Pertama", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            // Horizontal Question Number Bar
                            androidx.compose.foundation.lazy.LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(questions.size) { index ->
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
                                
                                // Quick Add Soal button at end of bar
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF102B5E).copy(alpha = 0.1f),
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clickable {
                                                questions.add(
                                                    QuizQuestion(
                                                        pertanyaan = "",
                                                        opsiA = "",
                                                        opsiB = "",
                                                        opsiC = "",
                                                        opsiD = "",
                                                        jawabanBenar = "A"
                                                    )
                                                )
                                                activeQuestionIndex = questions.size - 1
                                            }
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Add, contentDescription = "Tambah Soal Baru", tint = Color(0xFF102B5E))
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            if (activeQuestionIndex in questions.indices) {
                                val q = questions[activeQuestionIndex]
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Soal #${activeQuestionIndex + 1} dari ${questions.size}", fontWeight = FontWeight.Bold, color = Color(0xFF102B5E))
                                            IconButton(
                                                onClick = {
                                                    questions.removeAt(activeQuestionIndex)
                                                    if (activeQuestionIndex >= questions.size && questions.isNotEmpty()) {
                                                        activeQuestionIndex = questions.size - 1
                                                    } else if (questions.isEmpty()) {
                                                        activeQuestionIndex = 0
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
                                            }
                                        }

                                        // Pilihan Tipe Soal
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            listOf("pilihan_ganda" to "Pilihan Ganda", "essay" to "Esai").forEach { (type, label) ->
                                                val isSelected = q.tipeSoal == type
                                                val buttonColor = if (isSelected) Color(0xFF102B5E) else Color.Transparent
                                                val contentColor = if (isSelected) Color.White else Color.Gray
                                                Button(
                                                    onClick = { questions[activeQuestionIndex] = q.copy(tipeSoal = type) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = buttonColor,
                                                        contentColor = contentColor
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(vertical = 8.dp)
                                                ) {
                                                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = q.pertanyaan,
                                            onValueChange = { questions[activeQuestionIndex] = q.copy(pertanyaan = it) },
                                            label = { Text("Pertanyaan") },
                                            modifier = Modifier.fillMaxWidth().height(if (q.tipeSoal == "essay") 150.dp else 56.dp),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        // Question Supporting Image Selector Panel
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            val hasImage = !q.localImageUri.isNullOrBlank() || !q.imagePath.isNullOrBlank()
                                            if (hasImage) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(140.dp)
                                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    coil.compose.AsyncImage(
                                                        model = q.localImageUri ?: q.imagePath,
                                                        contentDescription = "Gambar Pertanyaan",
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clickable {
                                                                activeQuestionImageIndex = activeQuestionIndex
                                                                questionImageLauncher.launch("image/*")
                                                            },
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = q.localImageName ?: "Gambar pertanyaan terlampir",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray,
                                                        maxLines = 1,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    TextButton(
                                                        onClick = {
                                                            questions[activeQuestionIndex] = q.copy(
                                                                localImageUri = null,
                                                                localImageName = null,
                                                                localImageBytes = null,
                                                                imagePath = null
                                                            )
                                                        },
                                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                                    ) {
                                                        Text("Hapus Gambar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        activeQuestionImageIndex = activeQuestionIndex
                                                        questionImageLauncher.launch("image/*")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF102B5E).copy(alpha = 0.05f),
                                                        contentColor = Color(0xFF102B5E)
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Tambah Gambar Pendukung (Opsional)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        if (q.tipeSoal == "pilihan_ganda") {
                                            OutlinedTextField(
                                                value = q.opsiA ?: "",
                                                onValueChange = { questions[activeQuestionIndex] = q.copy(opsiA = it) },
                                                label = { Text("Pilihan A") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = q.opsiB ?: "",
                                                onValueChange = { questions[activeQuestionIndex] = q.copy(opsiB = it) },
                                                label = { Text("Pilihan B") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = q.opsiC ?: "",
                                                onValueChange = { questions[activeQuestionIndex] = q.copy(opsiC = it) },
                                                label = { Text("Pilihan C") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = q.opsiD ?: "",
                                                onValueChange = { questions[activeQuestionIndex] = q.copy(opsiD = it) },
                                                label = { Text("Pilihan D") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text("Jawaban Benar:", fontWeight = FontWeight.Bold, color = Color(0xFF102B5E))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    listOf("A", "B", "C", "D").forEach { option ->
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            RadioButton(
                                                                selected = q.jawabanBenar == option,
                                                                onClick = { questions[activeQuestionIndex] = q.copy(jawabanBenar = option) },
                                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF102B5E))
                                                            )
                                                            Text(option, fontWeight = FontWeight.Medium)
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
            }
        }
    }
}
