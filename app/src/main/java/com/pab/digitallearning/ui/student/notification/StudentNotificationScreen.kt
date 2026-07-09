package com.pab.digitallearning.ui.student.notification

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.digitallearning.data.model.StudentNotification
import com.pab.digitallearning.util.StudentNotificationPrefs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentNotificationScreen(
    viewModel: StudentNotificationViewModel,
    token: String?,
    onNotificationClick: (Long) -> Unit, // passes contentId
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { StudentNotificationPrefs(context) }
    val state by viewModel.notificationsState.collectAsState()

    var activeFilter by remember { mutableStateOf("all") } // "all" or "unread"
    var searchQuery by remember { mutableStateOf("") }

    // Multi-select states
    var isEditMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Long>() }

    val totalCount = remember(state) {
        if (state is StudentNotificationUiState.Success) {
            (state as StudentNotificationUiState.Success).data.size
        } else 0
    }

    val unreadCount = remember(state) {
        if (state is StudentNotificationUiState.Success) {
            (state as StudentNotificationUiState.Success).data.count { !it.isRead }
        } else 0
    }

    LaunchedEffect(key1 = token) {
        viewModel.fetchNotifications(token, prefs)
    }

    // Reset selection when exiting edit mode
    LaunchedEffect(isEditMode) {
        if (!isEditMode) {
            selectedIds.clear()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFD)) // Premium soft background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Filter Chips & Select All bar
            AnimatedContent(targetState = isEditMode, label = "HeaderTransition") { editMode ->
                if (editMode && state is StudentNotificationUiState.Success) {
                    val successState = state as StudentNotificationUiState.Success
                    val filteredNotifications = remember(successState.data, activeFilter, searchQuery) {
                        val base = if (activeFilter == "unread") {
                            successState.data.filter { !it.isRead }
                        } else {
                            successState.data
                        }
                        if (searchQuery.isNotEmpty()) {
                            base.filter {
                                it.message.contains(searchQuery, ignoreCase = true) ||
                                it.type.contains(searchQuery, ignoreCase = true)
                            }
                        } else {
                            base
                        }
                    }

                    val allSelected = filteredNotifications.isNotEmpty() && selectedIds.size == filteredNotifications.size

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = allSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedIds.clear()
                                        selectedIds.addAll(filteredNotifications.map { it.id })
                                    } else {
                                        selectedIds.clear()
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF102B5E),
                                    uncheckedColor = Color(0xFF9CA3AF),
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(
                                text = "Pilih Semua (${filteredNotifications.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${selectedIds.size} Terpilih",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF102B5E)
                            )

                            TextButton(
                                onClick = { isEditMode = false },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Batal",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Row 1: Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = activeFilter == "all",
                                onClick = { activeFilter = "all" },
                                label = { Text("Semua", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF102B5E),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF4B5563)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Box {
                                FilterChip(
                                    selected = activeFilter == "unread",
                                    onClick = { activeFilter = "unread" },
                                    label = { Text("Belum Dibaca", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF102B5E),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White,
                                        labelColor = Color(0xFF4B5563)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                if (unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 2.dp, y = (-2).dp)
                                            .size(8.dp)
                                            .background(Color(0xFFEF4444), CircleShape)
                                    )
                                }
                            }
                        }

                        // Row 2: Action Buttons
                        if (state is StudentNotificationUiState.Success) {
                            val successState = state as StudentNotificationUiState.Success
                            if (successState.data.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { isEditMode = !isEditMode },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFF102B5E)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pilih", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF102B5E))
                                    }

                                    val hasUnread = successState.data.any { !it.isRead }
                                    if (hasUnread) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        TextButton(
                                            onClick = {
                                                viewModel.markAllAsRead(prefs)
                                                Toast.makeText(context, "Semua notifikasi ditandai dibaca", Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color(0xFF102B5E)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Semua Dibaca", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF102B5E))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Main State List
            when (val resState = state) {
                is StudentNotificationUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF102B5E))
                    }
                }
                is StudentNotificationUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(resState.message, color = Color.Red, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchNotifications(token, prefs) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E))
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
                is StudentNotificationUiState.Success -> {
                    val filteredNotifications = remember(resState.data, activeFilter, searchQuery) {
                        val base = if (activeFilter == "unread") {
                            resState.data.filter { !it.isRead }
                        } else {
                            resState.data
                        }
                        if (searchQuery.isNotEmpty()) {
                            base.filter {
                                it.message.contains(searchQuery, ignoreCase = true) ||
                                it.type.contains(searchQuery, ignoreCase = true)
                            }
                        } else {
                            base
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Search Bar
                        if (resState.data.isNotEmpty()) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari notifikasi...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Cari",
                                        tint = Color.Gray
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Hapus",
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF102B5E),
                                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }

                        if (filteredNotifications.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Hasil pencarian tidak ditemukan"
                                           else if (activeFilter == "unread") "Tidak ada notifikasi yang belum dibaca"
                                           else "Kotak masuk notifikasi kosong",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Aktivitas baru seperti rilis tugas kuis atau pembagian nilai akan muncul di sini.",
                                        color = Color.Gray.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            val hasBottomPanel = isEditMode && selectedIds.isNotEmpty()
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    top = 12.dp,
                                    end = 16.dp,
                                    bottom = if (hasBottomPanel) 96.dp else 12.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredNotifications, key = { it.id }) { notification ->
                                    val isSelected = selectedIds.contains(notification.id)
                                    val density = LocalDensity.current
                                    val maxRevealWidth = 160.dp
                                    val maxRevealPx = remember { with(density) { maxRevealWidth.toPx() } }
                                    var dragOffset by remember(notification.id) { mutableStateOf(0f) }

                                    if (isEditMode) {
                                        // Disable swipe-to-dismiss when in edit mode to avoid gestures conflict
                                        StudentNotificationItemWithCheckbox(
                                            notification = notification,
                                            isSelected = isSelected,
                                            onSelectedChange = { checked ->
                                                if (checked) {
                                                    selectedIds.add(notification.id)
                                                } else {
                                                    selectedIds.remove(notification.id)
                                                }
                                            },
                                            onClick = {
                                                val checked = !isSelected
                                                if (checked) {
                                                    selectedIds.add(notification.id)
                                                } else {
                                                    selectedIds.remove(notification.id)
                                                }
                                            }
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(IntrinsicSize.Min)
                                        ) {
                                            // Background Content (Buttons)
                                            Row(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color(0xFFF3F4F6)),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // 1. Pilih Button
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .width(80.dp)
                                                        .background(Color(0xFF3B82F6))
                                                        .clickable {
                                                            isEditMode = true
                                                            selectedIds.clear()
                                                            selectedIds.add(notification.id)
                                                            dragOffset = 0f
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Pilih",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Pilih",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }

                                                // 2. Hapus Button
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .width(80.dp)
                                                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                                                        .background(Color(0xFFEF4444))
                                                        .clickable {
                                                            viewModel.deleteNotification(notification.id, prefs) { success, msg ->
                                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                            }
                                                            dragOffset = 0f
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Hapus",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Hapus",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }

                                            // Foreground Content (Card)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .offset { IntOffset(dragOffset.roundToInt(), 0) }
                                                    .pointerInput(notification.id) {
                                                        detectHorizontalDragGestures(
                                                            onHorizontalDrag = { change, dragAmount ->
                                                                change.consume()
                                                                val newOffset = (dragOffset + dragAmount).coerceIn(-maxRevealPx, 0f)
                                                                dragOffset = newOffset
                                                            },
                                                            onDragEnd = {
                                                                dragOffset = if (dragOffset < -maxRevealPx / 2f) -maxRevealPx else 0f
                                                            },
                                                            onDragCancel = {
                                                                dragOffset = if (dragOffset < -maxRevealPx / 2f) -maxRevealPx else 0f
                                                            }
                                                        )
                                                    }
                                            ) {
                                                StudentNotificationCard(
                                                    notification = notification,
                                                    isSelected = isSelected,
                                                    onClick = {
                                                        if (dragOffset == -maxRevealPx) {
                                                            dragOffset = 0f
                                                        } else {
                                                            viewModel.markAsRead(notification.id, prefs)
                                                            val contentId = notification.data?.contentId
                                                            if (contentId != null) {
                                                                onNotificationClick(contentId)
                                                            }
                                                        }
                                                    },
                                                    onActionClick = {
                                                        viewModel.markAsRead(notification.id, prefs)
                                                        val contentId = notification.data?.contentId
                                                        if (contentId != null) {
                                                            onNotificationClick(contentId)
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

        // Sticky Bottom Multi-Select Actions Panel
        AnimatedVisibility(
            visible = isEditMode && selectedIds.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.White,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedIds.size} Notifikasi Terpilih",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "Silakan pilih tindakan di bawah",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    // Delete multiple selected button
                    Button(
                        onClick = {
                            val idsToDelete = selectedIds.toList()
                            viewModel.deleteMultipleNotifications(idsToDelete, prefs) { success, msg ->
                                if (success) {
                                    isEditMode = false
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Mark multiple selected as read button
                    Button(
                        onClick = {
                            selectedIds.forEach { id ->
                                viewModel.markAsRead(id, prefs)
                            }
                            isEditMode = false
                            Toast.makeText(context, "Notifikasi terpilih ditandai dibaca", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dibaca", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentNotificationItemWithCheckbox(
    notification: StudentNotification,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF102B5E),
                uncheckedColor = Color(0xFF9CA3AF),
                checkmarkColor = Color.White
            ),
            modifier = Modifier.padding(end = 8.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            StudentNotificationCard(
                notification = notification,
                isSelected = isSelected,
                onClick = onClick,
                onActionClick = onClick
            )
        }
    }
}

@Composable
fun StudentNotificationCard(
    notification: StudentNotification,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val isGrading = notification.type.lowercase() == "grading"
    
    // Choose icon depending on content type
    val icon = when {
        isGrading -> Icons.Default.Check
        notification.data?.tipe == "kuis" -> Icons.Default.PlayArrow
        notification.data?.tipe == "tugas" -> Icons.Default.List
        else -> Icons.Default.Info
    }
    
    val iconBgColor = when {
        isGrading -> Color(0xFF10B981).copy(alpha = 0.15f)
        notification.data?.tipe == "kuis" -> Color(0xFFFF9500).copy(alpha = 0.15f)
        else -> Color(0xFF3B82F6).copy(alpha = 0.15f)
    }

    val iconColor = when {
        isGrading -> Color(0xFF10B981)
        notification.data?.tipe == "kuis" -> Color(0xFFFF9500)
        else -> Color(0xFF3B82F6)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF102B5E)) else null,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFF0F5FD) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else if (notification.isRead) 1.dp else 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Category Icon
                Surface(
                    shape = CircleShape,
                    color = iconBgColor,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Text content & action layout
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val label = when {
                            isGrading -> "Pemeriksaan Nilai"
                            notification.data?.tipe == "kuis" -> "Kuis Baru Rilis"
                            notification.data?.tipe == "tugas" -> "Tugas Baru Rilis"
                            notification.data?.tipe == "materi" -> "Materi Baru Rilis"
                            else -> "Pemberitahuan Kelas"
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = iconColor
                            )
                            if (!notification.isRead) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFFEF4444), CircleShape)
                                )
                            }
                        }

                        // Simple formatted date
                        val displayTime = notification.createdAt
                            .replace("T", " ")
                            .substringBefore(".")
                            .take(16)
                        Text(
                            text = displayTime,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notification.message,
                        fontSize = 13.sp,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        lineHeight = 18.sp
                    )

                    // Provide action button inside the card for dynamic direct navigation
                    val contentId = notification.data?.contentId
                    if (contentId != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onActionClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGrading) Color(0xFF10B981) else Color(0xFF102B5E)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val buttonText = if (isGrading) "Lihat Nilai" else "Buka Konten"
                                Text(
                                    text = buttonText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isGrading) Icons.Default.Star else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
