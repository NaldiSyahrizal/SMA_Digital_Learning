package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class TeacherDashboardResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: TeacherDashboardData
)

data class TeacherDashboardData(
    @SerializedName("total_classes") val totalClasses: Int,
    @SerializedName("total_subjects") val totalSubjects: Int,
    @SerializedName("total_students") val totalStudents: Int,
    @SerializedName("total_materials") val totalMaterials: Int,
    @SerializedName("total_assignments") val totalAssignments: Int,
    @SerializedName("total_quizzes") val totalQuizzes: Int,
    @SerializedName("total_contents") val totalContents: Int,
    @SerializedName("unread_notifications") val unreadNotifications: Int,
    @SerializedName("recent_contents") val recentContents: List<RecentContent>
)

data class RecentContent(
    @SerializedName("id") val id: Long,
    @SerializedName("tipe") val tipe: String, // "materi", "tugas", "kuis"
    @SerializedName("judul") val judul: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("created_at") val createdAt: String
)
