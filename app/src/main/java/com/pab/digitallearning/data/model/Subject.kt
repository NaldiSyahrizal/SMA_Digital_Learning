package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class Subject(
    val id: Long?,
    @SerializedName("kode_mapel")
    val kodeMapel: String?,
    val nama: String?,
    val kategori: String?,
    @SerializedName("tingkatan_id")
    val tingkatanId: Long?,
    @SerializedName("tingkatan_name")
    val tingkatanName: String?,
    @SerializedName("package_ids")
    val packageIds: List<Long>?,
    val packages: String?,
    @SerializedName("jam_pelajaran")
    val jamPelajaran: Int?
)
