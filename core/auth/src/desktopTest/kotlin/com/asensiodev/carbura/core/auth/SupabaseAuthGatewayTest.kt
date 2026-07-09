package com.asensiodev.carbura.core.auth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import kotlin.test.Test
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class SupabaseAuthGatewayTest {
    @Test
    fun currentSessionReturnsNullWhenNoSessionExists() = runTest {
        val client = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "anon-key",
        ) {
            install(Auth)
        }

        val gateway = SupabaseAuthGateway(client, googleClientId = "")

        assertNull(gateway.currentSession())
    }
}
