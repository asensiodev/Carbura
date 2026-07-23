package com.asensiodev.carbura.core.auth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNull

class SupabaseAuthGatewayTest {
    @Test
    fun currentSessionReturnsNullWhenNoSessionExists() =
        runTest {
            val client =
                createSupabaseClient(
                    supabaseUrl = "https://example.supabase.co",
                    supabaseKey = "anon-key",
                ) {
                    install(Auth)
                }

            val gateway = SupabaseAuthGateway(client)

            assertNull(gateway.currentSession())
        }
}
