package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class StudentSubjectResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<StudentSubject>
)

data class StudentSubject(
    @SerializedName("id") val id: Long,
    @SerializedName("kode_mapel") val kodeMapel: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("kategori") val kategori: String, // "umum" or "pilihan"
    @SerializedName("jam_pelajaran") val jamPelajaran: Int,
    @SerializedName("interest_score") val interestScore: Int
)
