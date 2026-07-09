package com.pab.digitallearning.ui.student.content

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed interface StudentContentDetailUiState {
    object Loading : StudentContentDetailUiState
    data class Success(val data: StudentContentDetailData) : StudentContentDetailUiState
    data class Error(val message: String) : StudentContentDetailUiState
}

sealed interface SubmitTaskUiState {
    object Idle : SubmitTaskUiState
    object Loading : SubmitTaskUiState
    data class Success(val message: String) : SubmitTaskUiState
    data class Error(val message: String) : SubmitTaskUiState
}

sealed interface SubmitQuizUiState {
    object Idle : SubmitQuizUiState
    object Loading : SubmitQuizUiState
    data class Success(val response: StudentQuizSubmitData) : SubmitQuizUiState
    data class Error(val message: String) : SubmitQuizUiState
}

class StudentContentViewModel : ViewModel() {

    private val _detailState = MutableStateFlow<StudentContentDetailUiState>(StudentContentDetailUiState.Loading)
    val detailState: StateFlow<StudentContentDetailUiState> = _detailState.asStateFlow()

    private val _submitTaskState = MutableStateFlow<SubmitTaskUiState>(SubmitTaskUiState.Idle)
    val submitTaskState: StateFlow<SubmitTaskUiState> = _submitTaskState.asStateFlow()

    private val _submitQuizState = MutableStateFlow<SubmitQuizUiState>(SubmitQuizUiState.Idle)
    val submitQuizState: StateFlow<SubmitQuizUiState> = _submitQuizState.asStateFlow()

    private val _commentsState = MutableStateFlow<List<com.pab.digitallearning.data.model.CommentItem>>(emptyList())
    val commentsState: StateFlow<List<com.pab.digitallearning.data.model.CommentItem>> = _commentsState.asStateFlow()

    private val _postCommentLoading = MutableStateFlow(false)
    val postCommentLoading: StateFlow<Boolean> = _postCommentLoading.asStateFlow()

    fun resetSubmitStates() {
        _submitTaskState.value = SubmitTaskUiState.Idle
        _submitQuizState.value = SubmitQuizUiState.Idle
    }

    fun fetchContentDetail(token: String?, contentId: Long) {
        if (token.isNullOrEmpty()) {
            _detailState.value = StudentContentDetailUiState.Error("Token tidak valid.")
            return
        }
        _detailState.value = StudentContentDetailUiState.Loading

        ApiClient.apiService.getStudentContentDetail("Bearer $token", contentId)
            .enqueue(object : Callback<StudentContentDetailResponse> {
                override fun onResponse(
                    call: Call<StudentContentDetailResponse>,
                    response: Response<StudentContentDetailResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _detailState.value = StudentContentDetailUiState.Success(response.body()!!.data)
                    } else {
                        _detailState.value = StudentContentDetailUiState.Error("Gagal memuat detail: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<StudentContentDetailResponse>, t: Throwable) {
                    _detailState.value = StudentContentDetailUiState.Error("Koneksi gagal: ${t.localizedMessage}")
                }
            })
    }

    fun submitTask(
        token: String?,
        contentId: Long,
        submissionText: String?,
        fileUri: Uri?,
        clearFile: Boolean,
        context: Context
    ) {
        if (token.isNullOrEmpty()) {
            _submitTaskState.value = SubmitTaskUiState.Error("Token tidak valid.")
            return
        }
        _submitTaskState.value = SubmitTaskUiState.Loading

        try {
            val contentIdBody = contentId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val submissionTextBody = submissionText?.toRequestBody("text/plain".toMediaTypeOrNull())
            val clearFileBody = (if (clearFile) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())

            var filePart: MultipartBody.Part? = null
            if (fileUri != null) {
                val (fileBytes, fileName) = getFileDetailsFromUri(context, fileUri)
                if (fileBytes != null && fileName != null) {
                    val fileRequestBody = fileBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                    filePart = MultipartBody.Part.createFormData("file", fileName, fileRequestBody)
                }
            }

            ApiClient.apiService.submitStudentTask("Bearer $token", contentIdBody, submissionTextBody, clearFileBody, filePart)
                .enqueue(object : Callback<BasicResponse> {
                    override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            if (body.status == "success") {
                                _submitTaskState.value = SubmitTaskUiState.Success(body.message)
                                fetchContentDetail(token, contentId) // Reload detail
                            } else {
                                _submitTaskState.value = SubmitTaskUiState.Error(body.message)
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val msg = try {
                                Gson().fromJson(errorBody, BasicResponse::class.java).message
                            } catch (e: Exception) {
                                "Gagal mengumpulkan tugas: ${response.code()}"
                            }
                            _submitTaskState.value = SubmitTaskUiState.Error(msg)
                        }
                    }

                    override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                        _submitTaskState.value = SubmitTaskUiState.Error("Koneksi gagal: ${t.localizedMessage}")
                    }
                })
        } catch (e: Exception) {
            _submitTaskState.value = SubmitTaskUiState.Error("Kesalahan memproses file: ${e.localizedMessage}")
        }
    }

    fun submitQuiz(
        token: String?,
        contentId: Long,
        answers: Map<Long, String>,
        exitCount: Int,
        exitLogs: List<String>
    ) {
        if (token.isNullOrEmpty()) {
            _submitQuizState.value = SubmitQuizUiState.Error("Token tidak valid.")
            return
        }
        _submitQuizState.value = SubmitQuizUiState.Loading

        val contentIdBody = contentId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        
        val answersJson = Gson().toJson(answers)
        val answersBody = answersJson.toRequestBody("application/json".toMediaTypeOrNull())

        val exitCountBody = exitCount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val exitLogsJson = Gson().toJson(exitLogs)
        val exitLogsBody = exitLogsJson.toRequestBody("application/json".toMediaTypeOrNull())

        ApiClient.apiService.submitStudentQuiz("Bearer $token", contentIdBody, answersBody, exitCountBody, exitLogsBody)
            .enqueue(object : Callback<StudentQuizSubmitResponse> {
                override fun onResponse(
                    call: Call<StudentQuizSubmitResponse>,
                    response: Response<StudentQuizSubmitResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _submitQuizState.value = SubmitQuizUiState.Success(response.body()!!.data)
                        fetchContentDetail(token, contentId) // Reload detail
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val msg = try {
                            Gson().fromJson(errorBody, BasicResponse::class.java).message
                        } catch (e: Exception) {
                            "Gagal menyimpan jawaban: ${response.code()}"
                        }
                        _submitQuizState.value = SubmitQuizUiState.Error(msg)
                    }
                }

                override fun onFailure(call: Call<StudentQuizSubmitResponse>, t: Throwable) {
                    _submitQuizState.value = SubmitQuizUiState.Error("Koneksi gagal: ${t.localizedMessage}")
                }
            })
    }

    fun deleteSubmission(
        token: String?,
        submissionId: Long,
        contentId: Long
    ) {
        if (token.isNullOrEmpty()) {
            _submitTaskState.value = SubmitTaskUiState.Error("Token tidak valid.")
            return
        }
        _submitTaskState.value = SubmitTaskUiState.Loading

        ApiClient.apiService.deleteStudentSubmission("Bearer $token", submissionId)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.status == "success") {
                            _submitTaskState.value = SubmitTaskUiState.Success(body.message)
                            fetchContentDetail(token, contentId) // Reload detail
                        } else {
                            _submitTaskState.value = SubmitTaskUiState.Error(body.message)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val msg = try {
                            Gson().fromJson(errorBody, BasicResponse::class.java).message
                        } catch (e: Exception) {
                            "Gagal menghapus pengumpulan: ${response.code()}"
                        }
                        _submitTaskState.value = SubmitTaskUiState.Error(msg)
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    _submitTaskState.value = SubmitTaskUiState.Error("Koneksi gagal: ${t.localizedMessage}")
                }
            })
    }

    private fun getFileDetailsFromUri(context: Context, uri: Uri): Pair<ByteArray?, String?> {
        return try {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            var name: String? = null
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
                cursor.close()
            }
            if (name == null) {
                name = uri.lastPathSegment
            }
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            Pair(bytes, name)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }
    fun fetchComments(token: String, contentId: Long) {
        ApiClient.apiService.getComments("Bearer $token", contentId.toInt()).enqueue(object : retrofit2.Callback<com.pab.digitallearning.data.model.CommentResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.pab.digitallearning.data.model.CommentResponse>,
                response: retrofit2.Response<com.pab.digitallearning.data.model.CommentResponse>
            ) {
                if (response.isSuccessful) {
                    _commentsState.value = response.body()?.data ?: emptyList()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.pab.digitallearning.data.model.CommentResponse>, t: Throwable) {
                // ignore
            }
        })
    }

    fun postComment(token: String, contentId: Long, komentar: String, imageUri: android.net.Uri?, context: android.content.Context) {
        if (komentar.isBlank() && imageUri == null) return
        _postCommentLoading.value = true
        
        val komentarBody = komentar.toRequestBody("text/plain".toMediaTypeOrNull())
        var imagePart: okhttp3.MultipartBody.Part? = null
        
        if (imageUri != null) {
            val (bytes, name) = getFileDetailsFromUri(context, imageUri)
            if (bytes != null && name != null) {
                val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                imagePart = okhttp3.MultipartBody.Part.createFormData("image", name, reqFile)
            }
        }

        ApiClient.apiService.addComment("Bearer $token", contentId.toInt(), komentarBody, imagePart).enqueue(object : retrofit2.Callback<com.pab.digitallearning.data.model.AddCommentResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.pab.digitallearning.data.model.AddCommentResponse>,
                response: retrofit2.Response<com.pab.digitallearning.data.model.AddCommentResponse>
            ) {
                _postCommentLoading.value = false
                if (response.isSuccessful) {
                    fetchComments(token, contentId) // reload comments
                } else {
                    android.widget.Toast.makeText(context, "Gagal mengirim komentar: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<com.pab.digitallearning.data.model.AddCommentResponse>, t: Throwable) {
                _postCommentLoading.value = false
                android.widget.Toast.makeText(context, "Terjadi kesalahan jaringan", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun editComment(token: String, contentId: Long, commentId: Int, newText: String, onResult: (Boolean, String) -> Unit) {
        ApiClient.apiService.editComment("Bearer $token", contentId.toInt(), commentId, newText).enqueue(object : retrofit2.Callback<com.pab.digitallearning.data.model.BasicResponse> {
            override fun onResponse(call: retrofit2.Call<com.pab.digitallearning.data.model.BasicResponse>, response: retrofit2.Response<com.pab.digitallearning.data.model.BasicResponse>) {
                if (response.isSuccessful) {
                    fetchComments(token, contentId)
                    onResult(true, "Komentar diperbarui")
                } else {
                    onResult(false, "Gagal memperbarui (Maks 2 menit)")
                }
            }
            override fun onFailure(call: retrofit2.Call<com.pab.digitallearning.data.model.BasicResponse>, t: Throwable) {
                onResult(false, "Terjadi kesalahan koneksi")
            }
        })
    }

    fun deleteComment(token: String, contentId: Long, commentId: Int, onResult: (Boolean, String) -> Unit) {
        ApiClient.apiService.deleteComment("Bearer $token", contentId.toInt(), commentId).enqueue(object : retrofit2.Callback<com.pab.digitallearning.data.model.BasicResponse> {
            override fun onResponse(call: retrofit2.Call<com.pab.digitallearning.data.model.BasicResponse>, response: retrofit2.Response<com.pab.digitallearning.data.model.BasicResponse>) {
                if (response.isSuccessful) {
                    fetchComments(token, contentId)
                    onResult(true, "Komentar dihapus")
                } else {
                    onResult(false, "Gagal menghapus komentar")
                }
            }
            override fun onFailure(call: retrofit2.Call<com.pab.digitallearning.data.model.BasicResponse>, t: Throwable) {
                onResult(false, "Terjadi kesalahan koneksi")
            }
        })
    }
}
