package com.asensiodev.carbura.core.domain.auth

data class AuthSession(
    val accessToken: String,
    val user: AuthUser,
)

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
)
