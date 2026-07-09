package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class StudentProfile(
    val id: Long?,
    @SerializedName("user_id")
    val userId: Long?,
    @SerializedName("nis")
    val nis: String?,
    @SerializedName("nama_lengkap")
    val namaLengkap: String?,
    @SerializedName("jenis_kelamin")
    val jenisKelamin: String?,
    @SerializedName("no_telp")
    val noTelp: String?,
    @SerializedName("foto_profile")
    val fotoProfile: String?,
    val username: String? = null,
    val email: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = true
)
