package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class StudentDashboardResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: StudentDashboardData
)

data class StudentDashboardData(
    @SerializedName("profile") val profile: StudentDashboardProfile,
    @SerializedName("stats") val stats: StudentDashboardStats,
    @SerializedName("priorities") val priorities: List<StudentPriorityTask>
)

data class StudentDashboardProfile(
    @SerializedName("id") val id: Long,
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("nis") val nis: String,
    @SerializedName("kelas") val kelas: String,
    @SerializedName("foto_profile") val fotoProfile: String?
)

data class StudentDashboardStats(
    @SerializedName("completed_tasks") val completedTasks: Int,
    @SerializedName("graded_tasks") val gradedTasks: Int,
    @SerializedName("average_grade") val averageGrade: Double,
    @SerializedName("total_pending_tasks") val totalPendingTasks: Int
)

data class StudentPriorityTask(
    @SerializedName("id") val id: Long,
    @SerializedName("tipe") val tipe: String, // "tugas" or "kuis"
    @SerializedName("judul") val judul: String,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("saw_score") val sawScore: Double,
    @SerializedName("attempts_count") val attemptsCount: Int,
    @SerializedName("quiz_max_attempts") val quizMaxAttempts: Int?,
    @SerializedName("quiz_duration_minutes") val quizDurationMinutes: Int?,
    @SerializedName("difficulty_desc") val difficultyDesc: String,
    @SerializedName("duration_desc") val durationDesc: String,
    @SerializedName("allowed_file_types") val allowedFileTypes: String?
)
