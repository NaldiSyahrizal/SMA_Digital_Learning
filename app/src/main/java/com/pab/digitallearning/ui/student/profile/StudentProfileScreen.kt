package com.pab.digitallearning.ui.student.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.pab.digitallearning.core.MainActivity
import com.pab.digitallearning.core.SessionManager
import com.pab.digitallearning.data.model.StudentProfileDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel,
    token: String?,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profileState by viewModel.profileState.collectAsState()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var isEditing by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showPickerOptions by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempPhotoUri != null) {
            selectedImageUri = tempPhotoUri
        }
    }

    LaunchedEffect(key1 = token) {
        viewModel.fetchProfile(token)
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = profileState) {
                is StudentProfileUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF102B5E))
                    }
                }
                is StudentProfileUiState.Success -> {
                    val profile = state.profile

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Avatar Header card
                        item {
                            ProfileHeaderCard(
                                profile = profile,
                                onEditPhotoClick = {
                                    showPickerOptions = true
                                }
                            )
                        }

                        // 2. Read-Only Academic metadata card
                        item {
                            AcademicDetailsCard(profile = profile)
                        }

                        // 3. Read-Only Settings card showing Username and Phone with "Edit Profil" button
                        item {
                            ReadOnlySettingsCard(
                                profile = profile,
                                onEditClick = { isEditing = true }
                            )
                        }

                        // 4. Logout action card
                        item {
                            OutlinedButton(
                                onClick = onLogoutClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF3B30)),
                                border = BorderStroke(1.dp, Color(0xFFFF3B30).copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Keluar Akun",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
                is StudentProfileUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
            }
        }
    }

    // Dialog Edit Profil
    if (isEditing) {
        if (profileState is StudentProfileUiState.Success) {
            val profile = (profileState as StudentProfileUiState.Success).profile
            var editUsername by remember { mutableStateOf(profile.username) }
            var editNoTelp by remember { mutableStateOf(profile.noTelp) }
            var editPassword by remember { mutableStateOf("") }
            var isSaving by remember { mutableStateOf(false) }

            var showOtpDialog by remember { mutableStateOf(false) }
            var otpStep by remember { mutableStateOf(1) }
            var resetToken by remember { mutableStateOf<String?>(null) }
            var otpCode by remember { mutableStateOf("") }
            var isSendingOtp by remember { mutableStateOf(false) }
            var isVerifyingOtp by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { if (!isSaving) isEditing = false },
                title = { Text("Edit Akun & Kontak", fontWeight = FontWeight.Bold, color = Color(0xFF102B5E)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it },
                            label = { Text("Nama Pengguna (Username)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF102B5E),
                                focusedLabelColor = Color(0xFF102B5E)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = editNoTelp,
                            onValueChange = { editNoTelp = it },
                            label = { Text("Nomor Telepon / WhatsApp") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF102B5E),
                                focusedLabelColor = Color(0xFF102B5E)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                isSendingOtp = true
                                viewModel.sendOtpRequest(
                                    email = profile.email,
                                    onResult = { success, msg ->
                                        isSendingOtp = false
                                        if (success) {
                                            Toast.makeText(context, "OTP dikirim ke ${profile.email}", Toast.LENGTH_SHORT).show()
                                            showOtpDialog = true
                                        } else {
                                            Toast.makeText(context, "Gagal mengirim OTP: $msg", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSendingOtp,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF102B5E)),
                            border = BorderStroke(1.dp, Color(0xFF102B5E).copy(alpha = 0.5f))
                        ) {
                            if (isSendingOtp) {
                                CircularProgressIndicator(color = Color(0xFF102B5E), modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ubah Kata Sandi Akun", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editUsername.isBlank() || editNoTelp.isBlank()) {
                                Toast.makeText(context, "Username dan Nomor HP wajib diisi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSaving = true
                            viewModel.updateProfile(
                                token = token,
                                username = editUsername,
                                noTelp = editNoTelp,
                                password = null
                            )
                            isSaving = false
                            isEditing = false
                        },
                        enabled = !isSaving && !isSendingOtp,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { isEditing = false },
                        enabled = !isSaving,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Batal")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )

            // Dialog Verifikasi OTP
            if (showOtpDialog) {
                var newPassword by remember { mutableStateOf("") }
                var confirmNewPassword by remember { mutableStateOf("") }
                var newPasswordVisible by remember { mutableStateOf(false) }
                var confirmNewPasswordVisible by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { 
                        if (!isVerifyingOtp) {
                            showOtpDialog = false 
                            otpStep = 1
                            otpCode = ""
                        }
                    },
                    title = { 
                        Text(
                            text = if (otpStep == 1) "Langkah 1: Verifikasi OTP" else "Langkah 2: Kata Sandi Baru", 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF102B5E)
                        ) 
                    },
                    text = {
                        Column {
                            if (otpStep == 1) {
                                Text(
                                    text = "Kode OTP telah dikirimkan ke email ${profile.email}. Kode ini hanya berlaku selama 5 menit. Silakan masukkan kode OTP tersebut di bawah ini untuk melanjutkan.",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { if (it.length <= 6) otpCode = it },
                                    label = { Text("Kode OTP (6 Digit)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF102B5E),
                                        focusedLabelColor = Color(0xFF102B5E)
                                    )
                                )
                            } else {
                                Text(
                                    text = "OTP berhasil diverifikasi! Silakan masukkan kata sandi baru akun Anda di bawah ini.",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("Kata Sandi Baru") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        val image = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                        val description = if (newPasswordVisible) "Sembunyikan password" else "Tampilkan password"
                                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                            Icon(imageVector = image, contentDescription = description, tint = Color(0xFF102B5E))
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF102B5E),
                                        focusedLabelColor = Color(0xFF102B5E)
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = confirmNewPassword,
                                    onValueChange = { confirmNewPassword = it },
                                    label = { Text("Konfirmasi Kata Sandi Baru") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = if (confirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        val image = if (confirmNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                        val description = if (confirmNewPasswordVisible) "Sembunyikan password" else "Tampilkan password"
                                        IconButton(onClick = { confirmNewPasswordVisible = !confirmNewPasswordVisible }) {
                                            Icon(imageVector = image, contentDescription = description, tint = Color(0xFF102B5E))
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF102B5E),
                                        focusedLabelColor = Color(0xFF102B5E)
                                    )
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (otpStep == 1) {
                                    if (otpCode.length != 6) {
                                        Toast.makeText(context, "Kode OTP harus 6 digit!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isVerifyingOtp = true
                                    viewModel.verifyOtp(
                                        email = profile.email,
                                        otp = otpCode,
                                        onResult = { success, tokenRes, verifyMsg ->
                                            isVerifyingOtp = false
                                            if (success && tokenRes != null) {
                                                resetToken = tokenRes
                                                otpStep = 2
                                            } else {
                                                Toast.makeText(context, "Verifikasi OTP gagal: $verifyMsg", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    )
                                } else {
                                    if (newPassword.length < 6) {
                                        Toast.makeText(context, "Kata sandi baru minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (newPassword != confirmNewPassword) {
                                        Toast.makeText(context, "Konfirmasi kata sandi tidak cocok!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isVerifyingOtp = true
                                    viewModel.resetPassword(
                                        email = profile.email,
                                        token = resetToken ?: "",
                                        passwordBaru = newPassword,
                                        onResult = { resetSuccess, resetMsg ->
                                            isVerifyingOtp = false
                                            if (resetSuccess) {
                                                Toast.makeText(context, "Kata sandi berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                                showOtpDialog = false
                                                isEditing = false
                                                otpStep = 1
                                                otpCode = ""
                                            } else {
                                                Toast.makeText(context, "Gagal memperbarui sandi: $resetMsg", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    )
                                }
                            },
                            enabled = !isVerifyingOtp,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isVerifyingOtp) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text(if (otpStep == 1) "Verifikasi OTP" else "Simpan Kata Sandi")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showOtpDialog = false 
                                otpStep = 1
                                otpCode = ""
                            },
                            enabled = !isVerifyingOtp,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                        ) {
                            Text("Batal")
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = Color.White
                )
            }
        }
    }

    // Dialog Peninjauan Foto Profil (Preview) dengan Fitur Geser & Zoom
    if (selectedImageUri != null) {
        var scale by remember(selectedImageUri) { mutableStateOf(1f) }
        var offset by remember(selectedImageUri) { mutableStateOf(Offset.Zero) }

        AlertDialog(
            onDismissRequest = { 
                if (!isUploadingPhoto) selectedImageUri = null 
            },
            title = { Text("Tinjau & Sesuaikan Foto", fontWeight = FontWeight.Bold, color = Color(0xFF102B5E)) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Geser untuk memindahkan dan cubit untuk memperbesar foto agar pas di dalam lingkaran:",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Circular Viewport Frame (Geser & Zoom)
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier
                            .size(200.dp)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 3f)
                                    offset = Offset(
                                        x = offset.x + pan.x,
                                        y = offset.y + pan.y
                                    )
                                }
                            },
                        shadowElevation = 6.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Peninjauan Foto",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    )
                            )
                        }
                    }

                    if (isUploadingPhoto) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = Color(0xFF102B5E))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isUploadingPhoto = true
                        val viewportSizePx = 200 * context.resources.displayMetrics.density
                        
                        viewModel.uploadProfilePicture(
                            context = context,
                            token = token,
                            imageUri = selectedImageUri!!,
                            scale = scale,
                            offsetX = offset.x,
                            offsetY = offset.y,
                            viewportSizePx = viewportSizePx,
                            onResult = { success, message ->
                                isUploadingPhoto = false
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    selectedImageUri = null
                                }
                            }
                        )
                    },
                    enabled = !isUploadingPhoto,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Foto")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedImageUri = null },
                    enabled = !isUploadingPhoto,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Dialog Pilihan Sumber Foto (Kamera / Galeri)
    if (showPickerOptions) {
        AlertDialog(
            onDismissRequest = { showPickerOptions = false },
            title = { Text("Pilih Sumber Foto", fontWeight = FontWeight.Bold, color = Color(0xFF102B5E)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Silakan pilih metode pengambilan foto profil Anda:",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Tombol Kamera
                    Button(
                        onClick = {
                            try {
                                val uri = getTempImageUri(context)
                                tempPhotoUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            showPickerOptions = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E), contentColor = Color.White),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("Ambil Foto dari Kamera", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Tombol Galeri
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch("image/*")
                            showPickerOptions = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF102B5E)),
                        border = BorderStroke(1.dp, Color(0xFF102B5E).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("Pilih dari Galeri Foto", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showPickerOptions = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun ProfileHeaderCard(
    profile: StudentProfileDetail,
    onEditPhotoClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photo with Edit overlay (Teacher system)
            Box(
                modifier = Modifier.size(108.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!profile.fotoProfile.isNullOrEmpty()) {
                        AsyncImage(
                            model = profile.fotoProfile,
                            contentDescription = "Foto Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val initials = profile.namaLengkap.trim().split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .map { it.firstOrNull()?.toString() ?: "" }
                            .joinToString("")
                            .uppercase()

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700), // Gold
                                            Color(0xFFFFA500)  // Amber
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials.ifEmpty { "?" },
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Floating Edit button with pencil icon
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF102B5E),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onEditPhotoClick() },
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Ubah Foto Profil",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val namaLengkap = profile.namaLengkap
            val adaptiveFontSize = when {
                namaLengkap.length > 24 -> 16.sp
                namaLengkap.length > 17 -> 19.sp
                else -> 24.sp
            }
            val adaptiveFontWeight = when {
                namaLengkap.length > 24 -> FontWeight.Bold
                else -> FontWeight.ExtraBold
            }

            Text(
                text = namaLengkap,
                fontSize = adaptiveFontSize,
                fontWeight = adaptiveFontWeight,
                color = Color(0xFF102B5E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            // No Class here under header as requested! Only Photo and Name!
        }
    }
}

@Composable
fun AcademicDetailsCard(profile: StudentProfileDetail) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Informasi Akademik Resmi",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF102B5E)
            )

            Spacer(modifier = Modifier.height(14.dp))

            DetailMetaRow(label = "Nomor Induk Siswa (NIS)", value = profile.nis)
            Divider(color = Color(0xFFF2F4F7), modifier = Modifier.padding(vertical = 10.dp))
            
            // Replaced Program Studi with Kelas (Paket) as requested!
            DetailMetaRow(label = "Kelas (Paket)", value = "${profile.kelas} (${profile.paketJurusan})")
            Divider(color = Color(0xFFF2F4F7), modifier = Modifier.padding(vertical = 10.dp))
            
            DetailMetaRow(label = "Jenis Kelamin", value = if (profile.jenisKelamin.uppercase() == "L") "Laki-laki" else "Perempuan")
            Divider(color = Color(0xFFF2F4F7), modifier = Modifier.padding(vertical = 10.dp))
            DetailMetaRow(label = "Alamat Email Terdaftar", value = profile.email)
        }
    }
}

@Composable
fun DetailMetaRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF102B5E)
        )
    }
}

@Composable
fun ReadOnlySettingsCard(
    profile: StudentProfileDetail,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pengaturan Akun & Kontak",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF102B5E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username Row
            StudentProfileInfoRow(icon = Icons.Default.Person, label = "Nama Pengguna (Username)", value = profile.username)
            
            // Phone Row
            StudentProfileInfoRow(icon = Icons.Default.Phone, label = "Nomor Telepon / WhatsApp", value = profile.noTelp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "*Nomor telepon dan username hanya bisa diperbarui melalui tombol Edit di bawah.",
                fontSize = 11.sp,
                color = Color.Gray,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Akun & Kontak",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun StudentProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF102B5E),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label, 
                color = Color.Gray, 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value, 
                fontWeight = FontWeight.SemiBold, 
                fontSize = 13.sp, 
                color = Color(0xFF1F2937)
            )
        }
    }
}

private fun getTempImageUri(context: android.content.Context): Uri {
    val tempFile = java.io.File.createTempFile("profile_capture", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
