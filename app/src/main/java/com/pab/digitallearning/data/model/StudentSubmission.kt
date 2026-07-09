package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class StudentSubmissionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<StudentSubmission>
)

data class StudentSubmission(
    @SerializedName("student_id") val studentId: Long,
    @SerializedName("student_name") val studentName: String,
    @SerializedName("nis") val nis: String,
    @SerializedName("submission_id") val submissionId: Long?,
    @SerializedName("submission_text") val submissionText: String?,
    @SerializedName("file_path") val filePath: String?,
    @SerializedName("nilai") val nilai: Int?,
    @SerializedName("catatan") val catatan: String?,
    @SerializedName("status") val status: String, // "not_submitted", "submitted", "graded"
    @SerializedName("quiz_answers") val quizAnswers: Map<String, String>? = null,
    @SerializedName("exit_count") val exitCount: Int? = 0,
    @SerializedName("exit_logs") val exitLogs: List<String>? = null,
    @SerializedName("updated_at") val updatedAt: String?
)
