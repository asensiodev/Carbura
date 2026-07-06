package com.asensiodev.carbura.core.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google

class SupabaseAuthGateway(
    private val client: SupabaseClient,
) : AuthGateway {
    override suspend fun currentSession(): AuthSession? =
        client.auth.currentSessionOrNull()?.let { session ->
            AuthSession(
                accessToken = session.accessToken,
                user = AuthUser(
                    id = session.user?.id.orEmpty(),
                    email = session.user?.email,
                    displayName = session.user?.userMetadata?.get("full_name")?.toString(),
                ),
            )
        }

    override suspend fun signInWithGoogle(): AuthSession {
        client.auth.signInWith(Google)
        return currentSession() ?: error("Google sign-in completed without an active Supabase session.")
    }

    override suspend fun signOut() {
        client.auth.signOut()
    }
}
