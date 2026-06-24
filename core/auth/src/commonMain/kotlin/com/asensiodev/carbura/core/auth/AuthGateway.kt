package com.asensiodev.carbura.core.auth

interface AuthGateway {
    suspend fun currentSession(): AuthSession?
    suspend fun signInWithGoogle(): AuthSession
    suspend fun signOut()
}

data class AuthSession(
    val accessToken: String,
    val user: AuthUser,
)

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
)
