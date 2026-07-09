package com.pab.digitallearning.data.model

data class LoginResponse(
    val status: String,
    val message: String,
    val token: String?,
    val role: String?
)
