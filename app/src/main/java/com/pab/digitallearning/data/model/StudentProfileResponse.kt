package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class StudentProfileResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: StudentProfileDetail
)

data class StudentProfileDetail(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("nis") val nis: String,
    @SerializedName("jenis_kelamin") val jenisKelamin: String,
    @SerializedName("no_telp") val noTelp: String,
    @SerializedName("kelas") val kelas: String,
    @SerializedName("paket_jurusan") val paketJurusan: String,
    @SerializedName("foto_profile") val fotoProfile: String? = null
)
