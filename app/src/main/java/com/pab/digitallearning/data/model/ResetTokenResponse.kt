package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class ResetTokenResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("reset_token") val resetToken: String?
)
