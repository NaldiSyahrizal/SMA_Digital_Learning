package com.pab.digitallearning.ui.teacher.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.data.model.TeacherProfile
import com.pab.digitallearning.data.model.ResetTokenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class TeacherProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun fetchProfile(token: String?) {
        if (token.isNullOrEmpty()) {
            _uiState.value = ProfileUiState.Error("Token tidak valid. Silakan login ulang.")
            return
        }

        _uiState.value = ProfileUiState.Loading

        ApiClient.apiService.getTeacherProfile("Bearer $token")
            .enqueue(object : Callback<TeacherProfile> {
                override fun onResponse(call: Call<TeacherProfile>, response: Response<TeacherProfile>) {
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = ProfileUiState.Success(response.body()!!)
                    } else {
                        _uiState.value = ProfileUiState.Error("Gagal mengambil data: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<TeacherProfile>, t: Throwable) {
                    _uiState.value = ProfileUiState.Error("Koneksi gagal: ${t.message}")
                }
            })
    }

    fun updateProfile(
        token: String?,
        username: String,
        noTelp: String,
        password: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.")
            return
        }

        val passParam = if (password.isNullOrBlank()) null else password

        ApiClient.apiService.updateTeacherProfile("Bearer $token", username, noTelp, passParam)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.status == "success") {
                            // Refresh profile data automatically
                            fetchProfile(token)
                            onResult(true, body.message)
                        } else {
                            onResult(false, body.message)
                        }
                    } else {
                        onResult(false, "Gagal memperbarui profil: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
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

            // 1. Decode Uri into Bitmap using modern ImageDecoder (perfect for SDK 28+)
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
            // Initial positioning: fit and center inside target canvas
            matrix.postScale(targetScale, targetScale)
            matrix.postTranslate(targetInitX, targetInitY)

            // Dynamic gestural adjustments: scale around target center and apply pan translations
            matrix.postScale(scale, scale, T / 2f, T / 2f)
            matrix.postTranslate(offsetX * factor, offsetY * factor)

            // Draw high-quality scaled & panned bitmap
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(srcBitmap, matrix, paint)

            // 3. Compress cropped bitmap to JPEG with standard high-fidelity quality (80%)
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

            ApiClient.apiService.uploadProfilePicture("Bearer $token", body)
                .enqueue(object : Callback<BasicResponse> {
                    override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val resBody = response.body()!!
                            if (resBody.status == "success") {
                                // Refresh profile after successful upload
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

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: TeacherProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
