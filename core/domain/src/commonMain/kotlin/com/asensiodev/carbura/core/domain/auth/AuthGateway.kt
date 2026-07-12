package com.asensiodev.carbura.core.domain.auth

interface AuthGateway {
    suspend fun currentSession(): AuthSession?

    suspend fun signInWithGoogle(): AuthSession

    suspend fun signInWithGoogle(idToken: String): AuthSession

    suspend fun signOut()
}
