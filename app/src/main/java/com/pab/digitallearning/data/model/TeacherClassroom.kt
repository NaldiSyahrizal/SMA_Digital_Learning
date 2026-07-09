package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class TeacherClassroomResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<TeacherClassroom>
)

data class TeacherClassroom(
    @SerializedName("id") val id: Long,
    @SerializedName("class_id") val classId: Long,
    @SerializedName("class_name") val className: String,
    @SerializedName("subject_id") val subjectId: Long,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("total_students") val totalStudents: Int
)

data class ClassStudentsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<ClassStudent>
)

data class ClassStudent(
    @SerializedName("id") val id: Long,
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("nis") val nis: String
)
