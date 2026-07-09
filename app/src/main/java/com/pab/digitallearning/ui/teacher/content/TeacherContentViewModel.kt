package com.pab.digitallearning.ui.teacher.content

import androidx.lifecycle.ViewModel
import android.content.Context
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.*
import okhttp3.ResponseBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope

class TeacherContentViewModel : ViewModel() {

    private val _classesState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val classesState: StateFlow<ClassesUiState> = _classesState.asStateFlow()

    private val _contentsState = MutableStateFlow<ContentsUiState>(ContentsUiState.Loading)
    val contentsState: StateFlow<ContentsUiState> = _contentsState.asStateFlow()

    private val _submissionsState = MutableStateFlow<SubmissionsUiState>(SubmissionsUiState.Loading)
    val submissionsState: StateFlow<SubmissionsUiState> = _submissionsState.asStateFlow()

    private val _studentsState = MutableStateFlow<StudentsUiState>(StudentsUiState.Loading)
    val studentsState: StateFlow<StudentsUiState> = _studentsState.asStateFlow()

    private val _contentDetailState = MutableStateFlow<ContentDetailUiState>(ContentDetailUiState.Loading)
    val contentDetailState: StateFlow<ContentDetailUiState> = _contentDetailState.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _commentsState = MutableStateFlow<List<com.pab.digitallearning.data.model.CommentItem>>(emptyList())
    val commentsState: StateFlow<List<com.pab.digitallearning.data.model.CommentItem>> = _commentsState.asStateFlow()

    private val _postCommentLoading = MutableStateFlow(false)
    val postCommentLoading: StateFlow<Boolean> = _postCommentLoading.asStateFlow()

    // 1. Fetch classrooms assigned to teacher
    fun fetchClasses(token: String?) {
        if (token.isNullOrEmpty()) {
            _classesState.value = ClassesUiState.Error("Token tidak valid.")
            return
        }
        _classesState.value = ClassesUiState.Loading

        ApiClient.apiService.getTeacherClassrooms("Bearer $token")
            .enqueue(object : Callback<TeacherClassroomResponse> {
                override fun onResponse(
                    call: Call<TeacherClassroomResponse>,
                    response: Response<TeacherClassroomResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _classesState.value = ClassesUiState.Success(response.body()!!.data)
                    } else {
                        _classesState.value = ClassesUiState.Error("Gagal memuat kelas: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<TeacherClassroomResponse>, t: Throwable) {
                    _classesState.value = ClassesUiState.Error("Koneksi gagal: ${t.message}")
                }
            })
    }

    // 2. Fetch contents (materials, tasks, quizzes)
    fun fetchContents(token: String?, classId: Long, subjectId: Long) {
        if (token.isNullOrEmpty()) {
            _contentsState.value = ContentsUiState.Error("Token tidak valid.")
            return
        }
        _contentsState.value = ContentsUiState.Loading

        ApiClient.apiService.getClassroomContents("Bearer $token", classId, subjectId)
            .enqueue(object : Callback<ClassroomContentResponse> {
                override fun onResponse(
                    call: Call<ClassroomContentResponse>,
                    response: Response<ClassroomContentResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _contentsState.value = ContentsUiState.Success(response.body()!!.data)
                    } else {
                        _contentsState.value = ContentsUiState.Error("Gagal memuat konten: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ClassroomContentResponse>, t: Throwable) {
                    _contentsState.value = ContentsUiState.Error("Koneksi gagal: ${t.message}")
                }
            })
    }

    // 3. Create new content (supports optional attachment file and quiz questions JSON)
    fun createContent(
        token: String?,
        classId: Long,
        classIds: List<Long>? = null,
        subjectId: Long,
        tipe: String,
        judul: String,
        deskripsi: String,
        dueDate: String?,
        isClosed: Boolean = false,
        closeAutomatically: Boolean = false,
        questions: List<QuizQuestion>?,
        weight: Int? = null,
        quizDurationMinutes: Int? = null,
        quizMaxAttempts: Int? = null,
        allowedFileTypes: String? = null,
        fileBytes: ByteArray?,
        fileName: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.")
            return
        }

        try {
            val classIdBody = classId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val classIdParts = classIds?.map { 
                it.toString().toRequestBody("text/plain".toMediaTypeOrNull()) 
            }
            val subjectIdBody = subjectId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val tipeBody = tipe.toRequestBody("text/plain".toMediaTypeOrNull())
            val judulBody = judul.toRequestBody("text/plain".toMediaTypeOrNull())
            val deskripsiBody = deskripsi.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val dueDateBody = dueDate?.toRequestBody("text/plain".toMediaTypeOrNull())
            val isClosedBody = (if (isClosed) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
            val closeAutomaticallyBody = (if (closeAutomatically) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Build questions JSON
            val gson = Gson()
            val questionsJson = if (!questions.isNullOrEmpty()) gson.toJson(questions) else null
            val questionsBody = questionsJson?.let { it.toRequestBody("application/json".toMediaTypeOrNull()) }

            val weightBody = if (tipe == "materi" || weight == null || weight == 0) null else weight.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val quizDurationMinutesBody = quizDurationMinutes?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val quizMaxAttemptsBody = quizMaxAttempts?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val allowedFileTypesBody = allowedFileTypes?.toRequestBody("text/plain".toMediaTypeOrNull())

            var filePart: MultipartBody.Part? = null
            if (fileBytes != null && fileName != null) {
                val fileRequestBody = fileBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                filePart = MultipartBody.Part.createFormData("file", fileName, fileRequestBody)
            }

            // Build question images parts
            val partsList = mutableListOf<MultipartBody.Part>()
            questions?.forEachIndexed { index, q ->
                if (q.localImageBytes != null && q.localImageName != null) {
                    val requestBody = q.localImageBytes.toRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("question_image_$index", q.localImageName, requestBody)
                    partsList.add(part)
                }
            }
            val questionImagesParts = if (partsList.isNotEmpty()) partsList else null

            ApiClient.apiService.createClassroomContent(
                token = "Bearer $token",
                classId = classIdBody,
                classIds = classIdParts,
                subjectId = subjectIdBody,
                tipe = tipeBody,
                judul = judulBody,
                deskripsi = deskripsiBody,
                dueDate = dueDateBody,
                isClosed = isClosedBody,
                closeAutomatically = closeAutomaticallyBody,
                questions = questionsBody,
                weight = weightBody,
                quizDurationMinutes = quizDurationMinutesBody,
                quizMaxAttempts = quizMaxAttemptsBody,
                allowedFileTypes = allowedFileTypesBody,
                file = filePart,
                questionImages = questionImagesParts
            ).enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.status == "success") {
                            onResult(true, "Konten berhasil disimpan!")
                            // Refresh content list
                            fetchContents(token, classId, subjectId)
                        } else {
                            onResult(false, body.message)
                        }
                    } else {
                        onResult(false, "Gagal menyimpan konten: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
        } catch (e: Exception) {
            onResult(false, "Error: ${e.message}")
        }
    }

    // 4. Delete content
    fun deleteContent(token: String?, contentId: Long, classId: Long, subjectId: Long, onResult: (Boolean, String) -> Unit) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.")
            return
        }

        ApiClient.apiService.deleteClassroomContent("Bearer $token", contentId)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.status == "success") {
                            onResult(true, "Konten berhasil dihapus!")
                            fetchContents(token, classId, subjectId)
                        } else {
                            onResult(false, body.message)
                        }
                    } else {
                        onResult(false, "Gagal menghapus konten: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
    }

    // 5. Fetch submissions
    fun fetchSubmissions(token: String?, contentId: Long) {
        if (token.isNullOrEmpty()) {
            _submissionsState.value = SubmissionsUiState.Error("Token tidak valid.")
            return
        }
        _submissionsState.value = SubmissionsUiState.Loading

        ApiClient.apiService.getStudentSubmissions("Bearer $token", contentId)
            .enqueue(object : Callback<StudentSubmissionResponse> {
                override fun onResponse(
                    call: Call<StudentSubmissionResponse>,
                    response: Response<StudentSubmissionResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _submissionsState.value = SubmissionsUiState.Success(response.body()!!.data)
                    } else {
                        _submissionsState.value = SubmissionsUiState.Error("Gagal memuat pengerjaan: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<StudentSubmissionResponse>, t: Throwable) {
                    _submissionsState.value = SubmissionsUiState.Error("Koneksi gagal: ${t.message}")
                }
            })
    }

    // 6. Grade submission
    fun gradeSubmission(
        token: String?,
        contentId: Long,
        submissionId: Long,
        nilai: Int,
        catatan: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.")
            return
        }

        ApiClient.apiService.gradeStudentSubmission("Bearer $token", submissionId, nilai, catatan)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.status == "success") {
                            onResult(true, "Penilaian berhasil disimpan!")
                            // Refresh submissions
                            fetchSubmissions(token, contentId)
                        } else {
                            onResult(false, body.message)
                        }
                    } else {
                        onResult(false, "Gagal menyimpan nilai: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
    }

    // 7. Fetch students enrolled in classroom
    fun fetchStudents(token: String?, classId: Long) {
        if (token.isNullOrEmpty()) {
            _studentsState.value = StudentsUiState.Error("Token tidak valid.")
            return
        }
        _studentsState.value = StudentsUiState.Loading

        ApiClient.apiService.getClassroomStudents("Bearer $token", classId)
            .enqueue(object : Callback<ClassStudentsResponse> {
                override fun onResponse(
                    call: Call<ClassStudentsResponse>,
                    response: Response<ClassStudentsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _studentsState.value = StudentsUiState.Success(response.body()!!.data)
                    } else {
                        _studentsState.value = StudentsUiState.Error("Gagal memuat siswa: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ClassStudentsResponse>, t: Throwable) {
                    _studentsState.value = StudentsUiState.Error("Koneksi gagal: ${t.message}")
                }
            })
    }

    // 8. Fetch specific content detail
    fun fetchContentDetail(token: String?, contentId: Long) {
        if (token.isNullOrEmpty()) {
            _contentDetailState.value = ContentDetailUiState.Error("Token tidak valid.")
            return
        }
        _contentDetailState.value = ContentDetailUiState.Loading

        ApiClient.apiService.getClassroomContentDetail("Bearer $token", contentId)
            .enqueue(object : Callback<ClassroomContentDetailResponse> {
                override fun onResponse(
                    call: Call<ClassroomContentDetailResponse>,
                    response: Response<ClassroomContentDetailResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _contentDetailState.value = ContentDetailUiState.Success(response.body()!!.data)
                    } else {
                        _contentDetailState.value = ContentDetailUiState.Error("Gagal memuat detail konten: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ClassroomContentDetailResponse>, t: Throwable) {
                    _contentDetailState.value = ContentDetailUiState.Error("Koneksi gagal: ${t.message}")
                }
            })
    }

    // 9. Update classroom content
    fun updateContent(
        token: String?,
        contentId: Long,
        judul: String,
        deskripsi: String,
        dueDate: String?,
        isClosed: Boolean = false,
        closeAutomatically: Boolean = false,
        questions: List<QuizQuestion>?,
        weight: Int? = null,
        quizDurationMinutes: Int? = null,
        quizMaxAttempts: Int? = null,
        allowedFileTypes: String? = null,
        classIds: List<Long>? = null,
        fileBytes: ByteArray?,
        fileName: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.")
            return
        }

        try {
            val methodBody = "PUT".toRequestBody("text/plain".toMediaTypeOrNull())
            val judulBody = judul.toRequestBody("text/plain".toMediaTypeOrNull())
            val deskripsiBody = deskripsi.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val dueDateBody = dueDate?.toRequestBody("text/plain".toMediaTypeOrNull())
            val isClosedBody = (if (isClosed) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
            val closeAutomaticallyBody = (if (closeAutomatically) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
            val classIdParts = classIds?.map { 
                it.toString().toRequestBody("text/plain".toMediaTypeOrNull()) 
            }
            
            // Build questions JSON
            val gson = Gson()
            val questionsJson = if (!questions.isNullOrEmpty()) gson.toJson(questions) else null
            val questionsBody = questionsJson?.let { it.toRequestBody("application/json".toMediaTypeOrNull()) }

            val weightBody = if (weight == null || weight == 0) null else weight.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val quizDurationMinutesBody = quizDurationMinutes?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val quizMaxAttemptsBody = quizMaxAttempts?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val allowedFileTypesBody = allowedFileTypes?.toRequestBody("text/plain".toMediaTypeOrNull())

            var filePart: MultipartBody.Part? = null
            if (fileBytes != null && fileName != null) {
                val fileRequestBody = fileBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                filePart = MultipartBody.Part.createFormData("file", fileName, fileRequestBody)
            }

            // Build question images parts
            val partsList = mutableListOf<MultipartBody.Part>()
            questions?.forEachIndexed { index, q ->
                if (q.localImageBytes != null && q.localImageName != null) {
                    val requestBody = q.localImageBytes.toRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("question_image_$index", q.localImageName, requestBody)
                    partsList.add(part)
                }
            }
            val questionImagesParts = if (partsList.isNotEmpty()) partsList else null

            ApiClient.apiService.updateClassroomContent(
                token = "Bearer $token",
                contentId = contentId,
                method = methodBody,
                judul = judulBody,
                deskripsi = deskripsiBody,
                dueDate = dueDateBody,
                isClosed = isClosedBody,
                closeAutomatically = closeAutomaticallyBody,
                questions = questionsBody,
                classIds = classIdParts,
                weight = weightBody,
                quizDurationMinutes = quizDurationMinutesBody,
                quizMaxAttempts = quizMaxAttemptsBody,
                allowedFileTypes = allowedFileTypesBody,
                file = filePart,
                questionImages = questionImagesParts
            ).enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.status == "success") {
                            onResult(true, "Konten berhasil diperbarui!")
                            fetchContentDetail(token, contentId)
                        } else {
                            onResult(false, body.message)
                        }
                    } else {
                        onResult(false, "Gagal memperbarui konten: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
        } catch (e: Exception) {
            onResult(false, "Error: ${e.message}")
        }
    }

    // 10. Toggle content closed state
    fun toggleContentClose(token: String?, contentId: Long, onResult: (Boolean, String) -> Unit) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.")
            return
        }

        ApiClient.apiService.toggleCloseClassroomContent("Bearer $token", contentId)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.status == "success") {
                            onResult(true, body.message)
                            fetchContentDetail(token, contentId)
                        } else {
                            onResult(false, body.message)
                        }
                    } else {
                        onResult(false, "Gagal mengubah status penutupan konten.")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
    }

    // 11. Export Class Gradebook (PDF)
    fun exportClassGradebook(
        token: String?, 
        classId: Long, 
        className: String, 
        subjectId: Long, 
        subjectName: String, 
        context: android.content.Context, 
        isForPrint: Boolean = false,
        onResult: (Boolean, String, java.io.File?) -> Unit
    ) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.", null)
            return
        }

        _isDownloading.value = true

        ApiClient.apiService.exportClassroomGrades("Bearer $token", classId, subjectId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                val body = response.body()!!
                                val safeClassName = className.replace(Regex("[^A-Za-z0-9]"), "_")
                                val safeSubjectName = subjectName.replace(Regex("[^A-Za-z0-9]"), "_")
                                val fileName = "Gradebook_${safeClassName}_${safeSubjectName}.pdf"
                                
                                if (isForPrint) {
                                    val tempFile = java.io.File(context.cacheDir, fileName)
                                    tempFile.outputStream().use { outputStream ->
                                        body.byteStream().use { inputStream ->
                                            inputStream.copyTo(outputStream)
                                        }
                                    }
                                    withContext(Dispatchers.Main) {
                                        _isDownloading.value = false
                                        onResult(true, "Siap dicetak", tempFile)
                                    }
                                } else {
                                    val resolver = context.contentResolver
                                    val contentValues = android.content.ContentValues().apply {
                                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                                    }
                                    
                                    val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                                    if (uri != null) {
                                        resolver.openOutputStream(uri)?.use { outputStream ->
                                            body.byteStream().use { inputStream ->
                                                inputStream.copyTo(outputStream)
                                            }
                                        }
                                        withContext(Dispatchers.Main) {
                                            _isDownloading.value = false
                                            onResult(true, "Berhasil diunduh ke folder Download: $fileName", null)
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            _isDownloading.value = false
                                            onResult(false, "Gagal membuat file di folder Download.", null)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    _isDownloading.value = false
                                    onResult(false, "Terjadi kesalahan saat menyimpan file: ${e.message}", null)
                                }
                            }
                        }
                    } else {
                        _isDownloading.value = false
                        onResult(false, "Gagal mengunduh data: ${response.code()}", null)
                    }
                }

                override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                    _isDownloading.value = false
                    onResult(false, "Koneksi gagal: ${t.message}", null)
                }
            })
    }
    fun fetchComments(token: String, contentId: Long) {
        ApiClient.apiService.getComments("Bearer $token", contentId.toInt()).enqueue(object : Callback<com.pab.digitallearning.data.model.CommentResponse> {
            override fun onResponse(
                call: Call<com.pab.digitallearning.data.model.CommentResponse>,
                response: Response<com.pab.digitallearning.data.model.CommentResponse>
            ) {
                if (response.isSuccessful) {
                    _commentsState.value = response.body()?.data ?: emptyList()
                }
            }
            override fun onFailure(call: Call<com.pab.digitallearning.data.model.CommentResponse>, t: Throwable) {
                // ignore
            }
        })
    }

    private fun getFileFromUri(context: android.content.Context, uri: android.net.Uri): Pair<ByteArray?, String?> {
        return try {
            val contentResolver = context.contentResolver
            var name: String? = null
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
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

    fun postComment(token: String, contentId: Long, komentar: String, imageUri: android.net.Uri?, context: android.content.Context) {
        if (komentar.isBlank() && imageUri == null) return
        _postCommentLoading.value = true
        
        val komentarBody = komentar.toRequestBody("text/plain".toMediaTypeOrNull())
        var imagePart: okhttp3.MultipartBody.Part? = null
        
        if (imageUri != null) {
            val (bytes, name) = getFileFromUri(context, imageUri)
            if (bytes != null && name != null) {
                val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                imagePart = okhttp3.MultipartBody.Part.createFormData("image", name, reqFile)
            }
        }

        ApiClient.apiService.addComment("Bearer $token", contentId.toInt(), komentarBody, imagePart).enqueue(object : Callback<com.pab.digitallearning.data.model.AddCommentResponse> {
            override fun onResponse(
                call: Call<com.pab.digitallearning.data.model.AddCommentResponse>,
                response: Response<com.pab.digitallearning.data.model.AddCommentResponse>
            ) {
                _postCommentLoading.value = false
                if (response.isSuccessful) {
                    fetchComments(token, contentId)
                } else {
                    android.widget.Toast.makeText(context, "Gagal mengirim komentar: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<com.pab.digitallearning.data.model.AddCommentResponse>, t: Throwable) {
                _postCommentLoading.value = false
                android.widget.Toast.makeText(context, "Terjadi kesalahan jaringan", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun editComment(token: String, contentId: Long, commentId: Int, newText: String, onResult: (Boolean, String) -> Unit) {
        ApiClient.apiService.editComment("Bearer $token", contentId.toInt(), commentId, newText).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (response.isSuccessful) {
                    fetchComments(token, contentId)
                    onResult(true, "Komentar diperbarui")
                } else {
                    onResult(false, "Gagal memperbarui (Maks 2 menit)")
                }
            }
            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                onResult(false, "Terjadi kesalahan koneksi")
            }
        })
    }

    fun deleteComment(token: String, contentId: Long, commentId: Int, onResult: (Boolean, String) -> Unit) {
        ApiClient.apiService.deleteComment("Bearer $token", contentId.toInt(), commentId).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (response.isSuccessful) {
                    fetchComments(token, contentId)
                    onResult(true, "Komentar dihapus")
                } else {
                    onResult(false, "Gagal menghapus komentar")
                }
            }
            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                onResult(false, "Terjadi kesalahan koneksi")
            }
        })
    }
}

sealed class ClassesUiState {
    object Loading : ClassesUiState()
    data class Success(val classes: List<TeacherClassroom>) : ClassesUiState()
    data class Error(val message: String) : ClassesUiState()
}

sealed class ContentsUiState {
    object Loading : ContentsUiState()
    data class Success(val contents: List<ClassroomContent>) : ContentsUiState()
    data class Error(val message: String) : ContentsUiState()
}

sealed class SubmissionsUiState {
    object Loading : SubmissionsUiState()
    data class Success(val submissions: List<StudentSubmission>) : SubmissionsUiState()
    data class Error(val message: String) : SubmissionsUiState()
}

sealed class StudentsUiState {
    object Loading : StudentsUiState()
    data class Success(val students: List<ClassStudent>) : StudentsUiState()
    data class Error(val message: String) : StudentsUiState()
}


sealed class ContentDetailUiState {
    object Loading : ContentDetailUiState()
    data class Success(val content: ClassroomContent) : ContentDetailUiState()
    data class Error(val message: String) : ContentDetailUiState()
}
