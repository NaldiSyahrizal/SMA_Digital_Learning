package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class NotificationListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<Notification>
)

data class Notification(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String, // "plotting", "submission", "profile_update"
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Map<String, String>?, // Navigational data: class_id, subject_id, content_id, etc.
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("is_graded") val isGraded: Boolean? = false,
    @SerializedName("created_at") val createdAt: String
)
