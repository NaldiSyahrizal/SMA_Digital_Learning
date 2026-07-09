package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class Tingkatan(
    val id: Long?,
    @SerializedName("nama_tingkatan")
    val namaTingkatan: String?
)
