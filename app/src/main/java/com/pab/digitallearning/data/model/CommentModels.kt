package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<CommentItem>
)

data class CommentItem(
    @SerializedName("id") val id: Int,
    @SerializedName("komentar") val komentar: String,
    @SerializedName("image_path") val imagePath: String?,
    @SerializedName("is_mine") val isMine: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("user") val user: CommentUser
)

data class CommentUser(
    @SerializedName("id") val id: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("nama_lengkap") val namaLengkap: String?,
    @SerializedName("foto_profile") val fotoProfile: String?,
    @SerializedName("role") val role: String
)

data class AddCommentResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CommentItem
)
