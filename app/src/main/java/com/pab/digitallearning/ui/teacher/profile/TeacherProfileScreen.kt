package com.pab.digitallearning.ui.teacher.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.digitallearning.core.MainActivity
import com.pab.digitallearning.core.SessionManager

@Composable
fun TeacherProfileScreen(viewModel: TeacherProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            is ProfileUiState.Loading -> {
                Box(modifier = Modifier.height(300.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF102B5E))
                }
            }
            is ProfileUiState.Error -> {
                val errorMsg = (uiState as ProfileUiState.Error).message
                Box(modifier = Modifier.height(300.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = errorMsg, color = Color.Red)
                }
            }
            is ProfileUiState.Success -> {
                val profile = (uiState as ProfileUiState.Success).profile
                
                // Kartu Profil Utama
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Foto Profil dengan Pencil Icon Overlay (Khusus Edit Foto)
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
                                    coil.compose.AsyncImage(
                                        model = profile.fotoProfile,
                                        contentDescription = "Foto Profil",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Foto Profil",
                                        tint = Color(0xFF102B5E),
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxSize()
                                    )
                                }
                            }
                            
                            // Floating Pencil Edit Icon (Khusus ubah foto profil)
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF102B5E),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        showPickerOptions = true
                                    },
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
                        
                        val namaLengkap = profile.namaLengkap ?: "-"
                        // Auto-shrink font size dynamically based on name length to ensure it fits on a single line
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
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = "NIP: ${profile.nip ?: "-"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Info Details dalam bentuk Horizontal Card Pills yang Premium
                        ProfileInfoRow(icon = Icons.Default.AccountCircle, label = "Username", value = profile.username ?: "-")
                        ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = profile.email ?: "-")
                        ProfileInfoRow(icon = Icons.Default.Phone, label = "No. Telepon", value = profile.noTelp ?: "-")
                        ProfileInfoRow(
                            icon = Icons.Default.Info, 
                            label = "Jenis Kelamin", 
                            value = if (profile.jenisKelamin == "L") "Laki-laki" else "Perempuan"
                        )
                        
                        // Keterangan / Deskripsi tombol edit
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "*Anda dapat memperbarui Username, Nomor HP, dan Password melalui tombol di bawah ini.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Tombol Edit Profil
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF102B5E),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Profil", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Tombol Logout
        OutlinedButton(
            onClick = {
                sessionManager.clearSession()
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
            border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar Aplikasi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    // Dialog Edit Profil
    if (isEditing) {
        if (uiState is ProfileUiState.Success) {
            val profile = (uiState as ProfileUiState.Success).profile
            var editUsername by remember { mutableStateOf(profile.username ?: "") }
            var editNoTelp by remember { mutableStateOf(profile.noTelp ?: "") }
            var editPassword by remember { mutableStateOf("") }
            var showOtpDialog by remember { mutableStateOf(false) }
            var otpStep by remember { mutableStateOf(1) }
            var resetToken by remember { mutableStateOf<String?>(null) }
            var otpCode by remember { mutableStateOf("") }
            var isSendingOtp by remember { mutableStateOf(false) }
            var isVerifyingOtp by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { isEditing = false },
                title = { Text("Edit Profil", fontWeight = FontWeight.Bold, color = Color(0xFF102B5E)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it },
                            label = { Text("Username") },
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
                            label = { Text("No. HP") },
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
                                    email = profile.email ?: "",
                                    onResult = { success, msg ->
                                        isSendingOtp = false
                                        if (success) {
                                            android.widget.Toast.makeText(context, "OTP dikirim ke ${profile.email}", android.widget.Toast.LENGTH_SHORT).show()
                                            showOtpDialog = true
                                        } else {
                                            android.widget.Toast.makeText(context, "Gagal mengirim OTP: $msg", android.widget.Toast.LENGTH_LONG).show()
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
                                android.widget.Toast.makeText(context, "Username dan Nomor HP tidak boleh kosong!", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.updateProfile(
                                token = sessionManager.getToken(),
                                username = editUsername,
                                noTelp = editNoTelp,
                                password = null,
                                onResult = { success, message ->
                                    if (success) {
                                        android.widget.Toast.makeText(context, "Profil berhasil diperbarui!", android.widget.Toast.LENGTH_SHORT).show()
                                        isEditing = false
                                    } else {
                                        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { isEditing = false },
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
                                    text = "Kode OTP telah dikirimkan ke email ${profile.email ?: ""}. Kode ini hanya berlaku selama 5 menit. Silakan masukkan kode OTP tersebut di bawah ini untuk melanjutkan.",
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
                                        android.widget.Toast.makeText(context, "Kode OTP harus 6 digit!", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isVerifyingOtp = true
                                    viewModel.verifyOtp(
                                        email = profile.email ?: "",
                                        otp = otpCode,
                                        onResult = { success, tokenRes, verifyMsg ->
                                            isVerifyingOtp = false
                                            if (success && tokenRes != null) {
                                                resetToken = tokenRes
                                                otpStep = 2
                                            } else {
                                                android.widget.Toast.makeText(context, "Verifikasi OTP gagal: $verifyMsg", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    )
                                } else {
                                    if (newPassword.length < 6) {
                                        android.widget.Toast.makeText(context, "Kata sandi baru minimal 6 karakter!", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (newPassword != confirmNewPassword) {
                                        android.widget.Toast.makeText(context, "Konfirmasi kata sandi tidak cocok!", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isVerifyingOtp = true
                                    viewModel.resetPassword(
                                        email = profile.email ?: "",
                                        token = resetToken ?: "",
                                        passwordBaru = newPassword,
                                        onResult = { resetSuccess, resetMsg ->
                                            isVerifyingOtp = false
                                            if (resetSuccess) {
                                                android.widget.Toast.makeText(context, "Kata sandi berhasil diperbarui!", android.widget.Toast.LENGTH_SHORT).show()
                                                showOtpDialog = false
                                                isEditing = false
                                                otpStep = 1
                                                otpCode = ""
                                            } else {
                                                android.widget.Toast.makeText(context, "Gagal memperbarui sandi: $resetMsg", android.widget.Toast.LENGTH_LONG).show()
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
        } else {
            isEditing = false
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
                            coil.compose.AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Peninjauan Foto",
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
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
                        // Calculate viewport size in pixels dynamically
                        val viewportSizePx = 200 * context.resources.displayMetrics.density
                        
                        viewModel.uploadProfilePicture(
                            context = context,
                            token = sessionManager.getToken(),
                            imageUri = selectedImageUri!!,
                            scale = scale,
                            offsetX = offset.x,
                            offsetY = offset.y,
                            viewportSizePx = viewportSizePx,
                            onResult = { success, message ->
                                isUploadingPhoto = false
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
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
                                android.widget.Toast.makeText(context, "Gagal membuka kamera: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF102B5E),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label, 
                color = Color.Gray, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value, 
                fontWeight = FontWeight.SemiBold, 
                fontSize = 14.sp, 
                color = Color(0xFF1F2937)
            )
        }
    }
}
