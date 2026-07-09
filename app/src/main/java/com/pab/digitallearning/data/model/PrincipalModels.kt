package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class PrincipalDashboardResponse(
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PrincipalDashboardData
)

data class PrincipalDashboardData(
    @SerializedName("total_tugas") val totalTugas: Int,
    @SerializedName("total_kuis") val totalKuis: Int,
    @SerializedName("teacher_participation") val teacherParticipation: ParticipationData,
    @SerializedName("student_participation") val studentParticipation: ParticipationData,
    @SerializedName("subject_interests") val subjectInterests: List<SubjectInterestData>,
    @SerializedName("tugas_stats") val tugasStats: List<TimeStatData>,
    @SerializedName("kuis_stats") val kuisStats: List<TimeStatData>
)

data class TimeStatData(
    @SerializedName("label") val label: String,
    @SerializedName("count") val count: Int
)

data class ParticipationData(
    @SerializedName("active") val active: Int,
    @SerializedName("total") val total: Int
)

data class SubjectInterestData(
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("avg_rating") val avgRating: Double,
    @SerializedName("voters") val voters: Int
)
