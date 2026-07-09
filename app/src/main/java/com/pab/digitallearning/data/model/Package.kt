package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class Package(
    val id: Long?,
    @SerializedName("nama_paket")
    val namaPaket: String?,
    val jurusan: String?,
    @SerializedName("tingkatan_id")
    val tingkatanId: Long?,
    @SerializedName("tingkatan_name")
    val tingkatanName: String?,
    val deskripsi: String?,
    val subjects: List<Subject>?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)
