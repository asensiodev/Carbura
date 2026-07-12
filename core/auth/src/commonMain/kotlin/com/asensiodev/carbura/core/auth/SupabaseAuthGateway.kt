package com.asensiodev.carbura.core.auth

import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.auth.AuthSession
import com.asensiodev.carbura.core.domain.auth.AuthUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class SupabaseAuthGateway(
    private val client: SupabaseClient,
    private val googleClientId: String,
) : AuthGateway {
    override suspend fun currentSession(): AuthSession? =
        client.auth.currentSessionOrNull()?.let { session ->
            AuthSession(
                accessToken = session.accessToken,
                user =
                    AuthUser(
                        id = session.user?.id.orEmpty(),
                        email = session.user?.email,
                        displayName =
                            session.user
                                ?.userMetadata
                                ?.get("full_name")
                                ?.jsonPrimitive
                                ?.contentOrNull,
                    ),
            )
        }

    override suspend fun signInWithGoogle(): AuthSession {
        client.auth.signInWith(Google)
        return currentSession() ?: error("Google sign-in completed without an active Supabase session.")
    }

    override suspend fun signInWithGoogle(idToken: String): AuthSession {
        client.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
        }
        return currentSession() ?: error("Google ID token sign-in completed without an active Supabase session.")
    }

    override suspend fun signOut() {
        client.auth.signOut()
    }
}
