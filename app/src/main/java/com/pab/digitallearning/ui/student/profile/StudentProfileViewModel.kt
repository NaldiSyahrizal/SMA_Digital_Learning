package com.pab.digitallearning.ui.student.profile

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.data.model.StudentProfileDetail
import com.pab.digitallearning.data.model.StudentProfileResponse
import com.pab.digitallearning.data.model.ResetTokenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull

sealed interface StudentProfileUiState {
    object Loading : StudentProfileUiState
    data class Success(val profile: StudentProfileDetail) : StudentProfileUiState
    data class Error(val message: String) : StudentProfileUiState
}

sealed interface ProfileUpdateUiState {
    object Idle : ProfileUpdateUiState
    object Loading : ProfileUpdateUiState
    data class Success(val message: String) : ProfileUpdateUiState
    data class Error(val message: String) : ProfileUpdateUiState
}

class StudentProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow<StudentProfileUiState>(StudentProfileUiState.Loading)
    val profileState: StateFlow<StudentProfileUiState> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<ProfileUpdateUiState>(ProfileUpdateUiState.Idle)
    val updateState: StateFlow<ProfileUpdateUiState> = _updateState.asStateFlow()

    fun resetUpdateState() {
        _updateState.value = ProfileUpdateUiState.Idle
    }

    fun fetchProfile(token: String?) {
        if (token.isNullOrEmpty()) {
            _profileState.value = StudentProfileUiState.Error("Token tidak ditemukan.")
            return
        }
        if (_profileState.value !is StudentProfileUiState.Success) {
            _profileState.value = StudentProfileUiState.Loading
        }

        ApiClient.apiService.getStudentProfile("Bearer $token")
            .enqueue(object : Callback<StudentProfileResponse> {
                override fun onResponse(
                    call: Call<StudentProfileResponse>,
                    response: Response<StudentProfileResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _profileState.value = StudentProfileUiState.Success(response.body()!!.data)
                    } else {
                        _profileState.value = StudentProfileUiState.Error("Gagal memuat profil: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<StudentProfileResponse>, t: Throwable) {
                    _profileState.value = StudentProfileUiState.Error("Koneksi gagal: ${t.localizedMessage}")
                }
            })
    }

    fun updateProfile(
        token: String?,
        username: String,
        noTelp: String,
        password: String?
    ) {
        if (token.isNullOrEmpty()) {
            _updateState.value = ProfileUpdateUiState.Error("Token tidak valid.")
            return
        }
        _updateState.value = ProfileUpdateUiState.Loading

        ApiClient.apiService.updateStudentProfile("Bearer $token", username, noTelp, password)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.status == "success") {
                            _updateState.value = ProfileUpdateUiState.Success(body.message)
                            fetchProfile(token) // Refresh profile data
                        } else {
                            _updateState.value = ProfileUpdateUiState.Error(body.message)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val msg = try {
                            Gson().fromJson(errorBody, BasicResponse::class.java).message
                        } catch (e: Exception) {
                            "Gagal memperbarui profil: ${response.code()}"
                        }
                        _updateState.value = ProfileUpdateUiState.Error(msg)
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    _updateState.value = ProfileUpdateUiState.Error("Koneksi gagal: ${t.localizedMessage}")
                }
            })
    }

    fun sendOtpRequest(email: String, onResult: (Boolean, String) -> Unit) {
        ApiClient.apiService.forgotPasswordRequest(email)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        onResult(body.status == "success", body.message)
                    } else {
                        onResult(false, "Gagal mengirim OTP: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
    }

    fun verifyOtp(email: String, otp: String, onResult: (Boolean, String?, String) -> Unit) {
        ApiClient.apiService.forgotPasswordVerify(email, otp)
            .enqueue(object : Callback<ResetTokenResponse> {
                override fun onResponse(call: Call<ResetTokenResponse>, response: Response<ResetTokenResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.status == "success") {
                            onResult(true, body.resetToken, body.message)
                        } else {
                            onResult(false, null, body.message)
                        }
                    } else {
                        onResult(false, null, "Verifikasi OTP gagal: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<ResetTokenResponse>, t: Throwable) {
                    onResult(false, null, "Koneksi gagal: ${t.message}")
                }
            })
    }

    fun resetPassword(email: String, token: String, passwordBaru: String, onResult: (Boolean, String) -> Unit) {
        ApiClient.apiService.forgotPasswordReset(email, token, passwordBaru)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        onResult(body.status == "success", body.message)
                    } else {
                        onResult(false, "Gagal mereset sandi: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
    }

    fun uploadProfilePicture(
        context: android.content.Context,
        token: String?,
        imageUri: android.net.Uri,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        viewportSizePx: Float,
        onResult: (Boolean, String) -> Unit
    ) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.")
            return
        }

        try {
            val contentResolver = context.contentResolver

            // 1. Decode Uri into Bitmap using modern ImageDecoder
            val source = android.graphics.ImageDecoder.createSource(contentResolver, imageUri)
            val srcBitmap = android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
            }

            // 2. Perform accurate scaling and panning based on viewport gestures
            val targetSize = 512
            val croppedBitmap = android.graphics.Bitmap.createBitmap(targetSize, targetSize, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(croppedBitmap)
            canvas.drawColor(android.graphics.Color.WHITE) // Background fill

            val W = srcBitmap.width.toFloat()
            val H = srcBitmap.height.toFloat()
            val T = targetSize.toFloat()
            val V = viewportSizePx

            // Calculate Fit aspect-ratio scale for target canvas
            val targetScale = Math.min(T / W, T / H)
            val targetFitW = W * targetScale
            val targetFitH = H * targetScale

            val targetInitX = (T - targetFitW) / 2f
            val targetInitY = (T - targetFitH) / 2f

            // Scale factor to map screen-space drag/pan to target 512x512 space
            val factor = T / V

            val matrix = android.graphics.Matrix()
            matrix.postScale(targetScale, targetScale)
            matrix.postTranslate(targetInitX, targetInitY)

            // Dynamic gestural adjustments
            matrix.postScale(scale, scale, T / 2f, T / 2f)
            matrix.postTranslate(offsetX * factor, offsetY * factor)

            // Draw high-quality scaled & panned bitmap
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(srcBitmap, matrix, paint)

            // 3. Compress cropped bitmap to JPEG (80%)
            val outputStream = java.io.ByteArrayOutputStream()
            croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            outputStream.close()

            croppedBitmap.recycle()
            srcBitmap.recycle()

            if (bytes == null || bytes.isEmpty()) {
                onResult(false, "Gagal mengompresi gambar.")
                return
            }

            // Create request body from compressed byte array
            val requestFile = okhttp3.RequestBody.create(
                "image/jpeg".toMediaTypeOrNull(),
                bytes
            )

            val body = okhttp3.MultipartBody.Part.createFormData("photo", "profile.jpg", requestFile)

            ApiClient.apiService.uploadStudentProfilePicture("Bearer $token", body)
                .enqueue(object : Callback<BasicResponse> {
                    override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val resBody = response.body()!!
                            if (resBody.status == "success") {
                                fetchProfile(token)
                                onResult(true, "Foto profil berhasil diperbarui!")
                            } else {
                                onResult(false, resBody.message)
                            }
                        } else {
                            onResult(false, "Gagal mengunggah gambar: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                        onResult(false, "Koneksi gagal: ${t.message}")
                    }
                })
        } catch (e: Exception) {
            onResult(false, "Terjadi kesalahan kompresi: ${e.message}")
        }
    }
}
