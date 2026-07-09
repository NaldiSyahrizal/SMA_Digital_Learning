package com.pab.digitallearning.ui.teacher.content

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.pab.digitallearning.core.SessionManager
import com.pab.digitallearning.data.model.ClassroomContent
import com.pab.digitallearning.data.model.QuizQuestion
import com.pab.digitallearning.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    viewModel: TeacherContentViewModel,
    classId: Long,
    className: String,
    subjectId: Long,
    subjectName: String,
    onContentSelected: (contentId: Long) -> Unit,
    onAddContentClicked: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val contentsState by viewModel.contentsState.collectAsState()
    val studentsState by viewModel.studentsState.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var studentSearchQuery by remember { mutableStateOf("") }
    var sortStudentsAscending by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.fetchContents(sessionManager.getToken(), classId, subjectId)
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            viewModel.fetchStudents(sessionManager.getToken(), classId)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onAddContentClicked,
                    containerColor = Color(0xFF102B5E),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Konten")
                }
            }
        },
        containerColor = Color(0xFFF8FAFD)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Elegant Navigation Tabs
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
                    text = { Text("Kelola Konten", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Daftar Siswa", fontWeight = FontWeight.Bold) }
                )
            }

            // Tab Content Switcher
            when (selectedTab) {
                0 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val state = contentsState) {
                            is ContentsUiState.Loading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF102B5E))
                                }
                            }
                            is ContentsUiState.Error -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(state.message, color = Color.Red, fontWeight = FontWeight.Medium)
                                }
                            }
                            is ContentsUiState.Success -> {
                                if (state.contents.isEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Belum ada konten pembelajaran.",
                                            color = Color.Gray,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Ketuk tombol + di kanan bawah untuk membuat materi pertama Anda.",
                                            color = Color.Gray.copy(alpha = 0.8f),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                } else {
                                    var contentSearchQuery by remember { mutableStateOf("") }
                                    // 0=Semua, 1=Materi, 2=Tugas, 3=Kuis
                                    var activeFilter by remember { mutableStateOf(0) }

                                    val filteredContents = remember(state.contents, contentSearchQuery, activeFilter) {
                                        state.contents.filter { content ->
                                            val matchesSearch = content.judul.contains(contentSearchQuery, ignoreCase = true) ||
                                                content.deskripsi.contains(contentSearchQuery, ignoreCase = true)
                                            val matchesType = when (activeFilter) {
                                                1 -> content.tipe == "materi"
                                                2 -> content.tipe == "tugas"
                                                3 -> content.tipe == "kuis"
                                                else -> true
                                            }
                                            matchesSearch && matchesType
                                        }
                                    }

                                    val filterLabels = listOf("Semua", "Materi", "Tugas", "Kuis")
                                    val filterColors = listOf(
                                        Color(0xFF102B5E),
                                        Color(0xFF10B981),
                                        Color(0xFF3B82F6),
                                        Color(0xFFF59E0B)
                                    )

                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        item {
                                            // Search Bar
                                            OutlinedTextField(
                                                value = contentSearchQuery,
                                                onValueChange = { contentSearchQuery = it },
                                                placeholder = { Text("Cari materi, tugas, atau kuis...") },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 4.dp),
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

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Filter Chips Row
                                            androidx.compose.foundation.lazy.LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                items(filterLabels.size) { index ->
                                                    val isActive = activeFilter == index
                                                    val chipColor = filterColors[index]
                                                    Surface(
                                                        shape = RoundedCornerShape(20.dp),
                                                        color = if (isActive) chipColor else Color.White,
                                                        border = androidx.compose.foundation.BorderStroke(
                                                            1.dp,
                                                            if (isActive) chipColor else Color.LightGray.copy(alpha = 0.5f)
                                                        ),
                                                        modifier = Modifier.clickable { activeFilter = index }
                                                    ) {
                                                        Text(
                                                            text = filterLabels[index],
                                                            color = if (isActive) Color.White else Color.Gray,
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (filteredContents.isEmpty()) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(24.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Tidak ditemukan konten yang cocok.",
                                                        color = Color.Gray,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        } else {
                                            items(filteredContents) { content ->
                                                ContentCard(
                                                    content = content,
                                                    onCardClick = {
                                                        onContentSelected(content.id)
                                                    },
                                                    onDeleteClick = {
                                                        viewModel.deleteContent(
                                                            token = sessionManager.getToken(),
                                                            contentId = content.id,
                                                            classId = classId,
                                                            subjectId = subjectId,
                                                            onResult = { success, msg ->
                                                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                                            }
                                                        )
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
                1 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        when (val stdState = studentsState) {
                            is StudentsUiState.Loading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF102B5E))
                                }
                            }
                            is StudentsUiState.Error -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(stdState.message, color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                            is StudentsUiState.Success -> {
                                val processedStudents = remember(stdState.students, studentSearchQuery, sortStudentsAscending) {
                                    val filtered = stdState.students.filter {
                                        it.namaLengkap.contains(studentSearchQuery, ignoreCase = true) ||
                                        it.nis.contains(studentSearchQuery, ignoreCase = true)
                                    }
                                    if (sortStudentsAscending) {
                                        filtered.sortedBy { it.namaLengkap.lowercase() }
                                    } else {
                                        filtered.sortedByDescending { it.namaLengkap.lowercase() }
                                    }
                                }

                                if (stdState.students.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Tidak ada siswa di kelas ini.", color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        item {
                                            Text(
                                                text = "Daftar Siswa Kelas (${processedStudents.size})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF102B5E),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }

                                        item {
                                            // Search Bar & Sort Toggle Row
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Action Buttons Row
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    // Cetak Button
                                                    Button(
                                                        onClick = {
                                                            android.widget.Toast.makeText(context, "Mempersiapkan PDF untuk dicetak...", android.widget.Toast.LENGTH_SHORT).show()
                                                            viewModel.exportClassGradebook(
                                                                sessionManager.getToken(),
                                                                classId,
                                                                className,
                                                                subjectId,
                                                                subjectName,
                                                                context,
                                                                true
                                                            ) { success, message, file ->
                                                                if (success && file != null) {
                                                                    val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
                                                                    val printAdapter = object : android.print.PrintDocumentAdapter() {
                                                                        override fun onLayout(old: android.print.PrintAttributes?, new: android.print.PrintAttributes?, cancel: android.os.CancellationSignal?, callback: android.print.PrintDocumentAdapter.LayoutResultCallback?, extras: android.os.Bundle?) {
                                                                            if (cancel?.isCanceled == true) { callback?.onLayoutCancelled(); return }
                                                                            val info = android.print.PrintDocumentInfo.Builder(file.name).setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build()
                                                                            callback?.onLayoutFinished(info, true)
                                                                        }
                                                                        override fun onWrite(pages: Array<out android.print.PageRange>?, dest: android.os.ParcelFileDescriptor?, cancel: android.os.CancellationSignal?, callback: android.print.PrintDocumentAdapter.WriteResultCallback?) {
                                                                            try {
                                                                                java.io.FileInputStream(file).use { input ->
                                                                                    java.io.FileOutputStream(dest?.fileDescriptor).use { output -> input.copyTo(output) }
                                                                                }
                                                                                callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                                                                            } catch (e: Exception) { callback?.onWriteFailed(e.message) }
                                                                        }
                                                                    }
                                                                    printManager.print("Gradebook_${className}_${subjectName}", printAdapter, null)
                                                                } else {
                                                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                                                                }
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFF102B5E),
                                                            contentColor = Color.White
                                                        ),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                                        modifier = Modifier.height(48.dp).weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Share, // Using Share icon as Print icon substitute
                                                            contentDescription = "Cetak",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Cetak", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }

                                                    // Unduh Button
                                                    Button(
                                                        onClick = {
                                                            android.widget.Toast.makeText(context, "Mengunduh file...", android.widget.Toast.LENGTH_SHORT).show()
                                                            viewModel.exportClassGradebook(
                                                                sessionManager.getToken(),
                                                                classId,
                                                                className,
                                                                subjectId,
                                                                subjectName,
                                                                context,
                                                                false
                                                            ) { success, message, _ ->
                                                                android.widget.Toast.makeText(context, message, if (success) android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFFF3F4F6),
                                                            contentColor = Color(0xFF102B5E)
                                                        ),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                                        modifier = Modifier.height(48.dp).weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.KeyboardArrowDown,
                                                            contentDescription = "Unduh",
                                                            modifier = Modifier.size(16.dp),
                                                            tint = Color(0xFF102B5E)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Unduh", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }

                                                    // Sort Toggle Button
                                                    Button(
                                                        onClick = { sortStudentsAscending = !sortStudentsAscending },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFF102B5E).copy(alpha = 0.1f),
                                                            contentColor = Color(0xFF102B5E)
                                                        ),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                                        modifier = Modifier.height(48.dp)
                                                    ) {
                                                        Text(
                                                            text = if (sortStudentsAscending) "A-Z" else "Z-A",
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }

                                                // Search Bar
                                                OutlinedTextField(
                                                    value = studentSearchQuery,
                                                    onValueChange = { studentSearchQuery = it },
                                                    placeholder = { Text("Cari nama/nis siswa...", fontSize = 13.sp) },
                                                    modifier = Modifier.fillMaxWidth(),
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
                                                        if (studentSearchQuery.isNotEmpty()) {
                                                            IconButton(onClick = { studentSearchQuery = "" }) {
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
                                            }
                                        }

                                        if (processedStudents.isEmpty()) {
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
                                            items(processedStudents) { student ->
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color(0xFF102B5E).copy(alpha = 0.1f),
                                                            modifier = Modifier.size(40.dp)
                                                        ) {
                                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF102B5E))
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        Column {
                                                            Text(student.namaLengkap, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                                                            Text("NIS: ${student.nis}", fontSize = 12.sp, color = Color.Gray)
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

@Composable
fun ContentCard(
    content: ClassroomContent,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val tipeColor = when (content.tipe) {
        "materi" -> Color(0xFF10B981) // Emerald Green
        "tugas" -> Color(0xFF3B82F6) // Ocean Blue
        else -> Color(0xFFF59E0B) // Amber Yellow
    }

    val typeLabel = content.tipe.replaceFirstChar { it.uppercase() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Type Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tipeColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = typeLabel,
                        color = tipeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Delete Button
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red.copy(alpha = 0.6f))
                }
            }

            Text(
                text = content.judul,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Text(
                text = content.deskripsi,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 2,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            val isCompleted = content.totalStudents > 0 && content.totalSubmissions >= content.totalStudents

            if (isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF10B981).copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Semua siswa di kelas sudah mengisi konten(${content.tipe})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            } else if (content.tipe != "materi" && !content.dueDate.isNullOrBlank()) {
                val isClosed = DateTimeUtils.isContentClosed(content.dueDate, content.isClosed)
                val countdown = DateTimeUtils.getCountdownString(content.dueDate)
                val dateFormatted = "${content.dueDate.substringBefore("T")} ${content.dueDate.substringAfter("T").take(5)}"
                
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isClosed) Color.Red.copy(alpha = 0.05f) else Color(0xFF3B82F6).copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isClosed) Color.Red.copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isClosed) Color.Red else Color(0xFF3B82F6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tenggat: $dateFormatted",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                text = if (isClosed) {
                                    if (content.isClosed) "Ditutup (Manual)" else "Tenggat Terlewati"
                                } else countdown,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isClosed) Color.Red else Color(0xFF3B82F6)
                            )
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isClosed) Color.Red else Color(0xFF10B981)
                        ) {
                            Text(
                                text = if (isClosed) "CLOSED" else "OPEN",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (content.tipe != "materi") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Progres Pengerjaan Siswa", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "${content.totalSubmissions} dari ${content.totalStudents} Siswa",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF102B5E)
                        )
                    }
                    Button(
                        onClick = onCardClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Buka Nilai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// AddContentDialog removed as it is now a full screen activity
