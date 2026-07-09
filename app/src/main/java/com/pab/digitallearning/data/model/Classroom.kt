package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class Classroom(
    val id: Long?,
    @SerializedName("nama_kelas")
    val namaKelas: String?,
    @SerializedName("tingkatan_id")
    val tingkatanId: Long?,
    @SerializedName("tingkatan_name")
    val tingkatanName: String?,
    val tingkat: String?,
    @SerializedName("wali_kelas_id")
    val waliKelasId: Long?,
    @SerializedName("wali_kelas_name")
    val waliKelasName: String?,
    @SerializedName("package_id")
    val packageId: Long?,
    @SerializedName("package_name")
    val packageName: String?,
    @SerializedName("total_siswa")
    val totalSiswa: Int = 0
)
