package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class ClassroomContentResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<ClassroomContent>
)

data class ClassroomContentDetailResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ClassroomContent
)

data class ClassroomContent(
    @SerializedName("id") val id: Long,
    @SerializedName("tipe") val tipe: String, // "materi", "tugas", "kuis"
    @SerializedName("judul") val judul: String,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("file_path") val filePath: String?,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("is_closed") val isClosed: Boolean = false,
    @SerializedName("close_automatically") val closeAutomatically: Boolean = false,
    @SerializedName("total_students") val totalStudents: Int,
    @SerializedName("total_submissions") val totalSubmissions: Int,
    @SerializedName("total_graded") val totalGraded: Int,
    @SerializedName("questions") val questions: List<QuizQuestion> = emptyList(),
    @SerializedName("active_group_class_ids") val activeGroupClassIds: List<Long>? = null,
    @SerializedName("difficulty") val difficulty: Int? = 3,
    @SerializedName("weight") val weight: Int? = 10,
    @SerializedName("estimated_duration") val estimatedDuration: Int? = 2,
    @SerializedName("quiz_duration_minutes") val quizDurationMinutes: Int? = null,
    @SerializedName("quiz_max_attempts") val quizMaxAttempts: Int? = 1,
    @SerializedName("allowed_file_types") val allowedFileTypes: String? = "all",
    @SerializedName("created_at") val createdAt: String
)

data class QuizQuestion(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("tipe_soal") val tipeSoal: String = "pilihan_ganda",
    @SerializedName("pertanyaan") val pertanyaan: String = "",
    @SerializedName("opsi_a") val opsiA: String? = "",
    @SerializedName("opsi_b") val opsiB: String? = "",
    @SerializedName("opsi_c") val opsiC: String? = "",
    @SerializedName("opsi_d") val opsiD: String? = "",
    @SerializedName("jawaban_benar") val jawabanBenar: String? = "A",
    @SerializedName("image_path") val imagePath: String? = null,
    val localImageUri: String? = null,
    val localImageName: String? = null,
    @kotlin.jvm.Transient val localImageBytes: ByteArray? = null
)
