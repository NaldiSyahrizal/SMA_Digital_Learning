package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    val status: String,
    val data: DashboardData
)

data class DashboardData(
    @SerializedName("total_guru") val totalGuru: Int,
    @SerializedName("total_siswa") val totalSiswa: Int,
    @SerializedName("total_kelas") val totalKelas: Int,
    @SerializedName("total_mapel") val totalMapel: Int
)
