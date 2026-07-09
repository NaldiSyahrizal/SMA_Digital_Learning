package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class StudentContentsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<StudentContentItem>
)

data class StudentContentItem(
    @SerializedName("id") val id: Long,
    @SerializedName("tipe") val tipe: String, // "materi", "tugas", "kuis"
    @SerializedName("judul") val judul: String,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("file_path") val filePath: String?,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("is_closed") val isClosed: Boolean,
    @SerializedName("close_automatically") val closeAutomatically: Boolean = false,
    @SerializedName("quiz_duration_minutes") val quizDurationMinutes: Int?,
    @SerializedName("quiz_max_attempts") val quizMaxAttempts: Int?,
    @SerializedName("allowed_file_types") val allowedFileTypes: String?,
    @SerializedName("submission_count") val submissionCount: Int,
    @SerializedName("submission_status") val submissionStatus: String, // "not_submitted", "submitted", "graded"
    @SerializedName("best_score") val bestScore: Int?,
    @SerializedName("created_at") val createdAt: String
)

data class StudentContentDetailResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: StudentContentDetailData
)

data class StudentContentDetailData(
    @SerializedName("id") val id: Long,
    @SerializedName("tipe") val tipe: String,
    @SerializedName("judul") val judul: String,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("file_path") val filePath: String?,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("is_closed") val isClosed: Boolean,
    @SerializedName("close_automatically") val closeAutomatically: Boolean = false,
    @SerializedName("quiz_duration_minutes") val quizDurationMinutes: Int?,
    @SerializedName("quiz_max_attempts") val quizMaxAttempts: Int?,
    @SerializedName("allowed_file_types") val allowedFileTypes: String?,
    @SerializedName("questions") val questions: List<StudentQuizQuestion>,
    @SerializedName("submissions") val submissions: List<StudentAttemptSubmission>
)

data class StudentQuizQuestion(
    @SerializedName("id") val id: Long,
    @SerializedName("tipe_soal") val tipeSoal: String = "pilihan_ganda",
    @SerializedName("pertanyaan") val pertanyaan: String = "",
    @SerializedName("opsi_a") val opsiA: String? = "",
    @SerializedName("opsi_b") val opsiB: String? = "",
    @SerializedName("opsi_c") val opsiC: String? = "",
    @SerializedName("opsi_d") val opsiD: String? = "",
    @SerializedName("image_path") val imagePath: String? = null,
    var selectedAnswer: String? = null // local selection state for Compose quiz playing
)

data class StudentAttemptSubmission(
    @SerializedName("id") val id: Long,
    @SerializedName("submission_text") val submissionText: String?,
    @SerializedName("file_path") val filePath: String?,
    @SerializedName("nilai") val nilai: Int?,
    @SerializedName("catatan") val catatan: String?,
    @SerializedName("status") val status: String, // "submitted" or "graded"
    @SerializedName("attempt_number") val attemptNumber: Int,
    @SerializedName("exit_count") val exitCount: Int? = 0,
    @SerializedName("exit_logs") val exitLogs: List<String>? = null,
    @SerializedName("updated_at") val updatedAt: String
)

data class StudentQuizSubmitResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StudentQuizSubmitData
)

data class StudentQuizSubmitData(
    @SerializedName("submission_id") val submissionId: Long,
    @SerializedName("nilai") val nilai: Int,
    @SerializedName("attempt_number") val attemptNumber: Int,
    @SerializedName("correct_answers") val correctAnswers: Int,
    @SerializedName("total_questions") val totalQuestions: Int
)
