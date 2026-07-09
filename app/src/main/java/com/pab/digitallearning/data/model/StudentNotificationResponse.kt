package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class StudentNotificationListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<StudentNotification>
)

data class StudentNotification(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String, // "publication" or "grading"
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StudentNotificationPayload?,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class StudentNotificationPayload(
    @SerializedName("content_id") val contentId: Long,
    @SerializedName("subject_id") val subjectId: Long,
    @SerializedName("tipe") val tipe: String?,
    @SerializedName("nilai") val nilai: Int?
)
