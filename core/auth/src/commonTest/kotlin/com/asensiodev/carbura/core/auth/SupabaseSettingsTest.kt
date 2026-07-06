package com.asensiodev.carbura.core.auth

import kotlin.test.Test
import kotlin.test.assertFailsWith

class SupabaseSettingsTest {
    @Test
    fun validateFailsWhenUrlIsBlank() {
        assertFailsWith<IllegalArgumentException> {
            SupabaseSettings(
                url = "",
                anonKey = "anon-key",
                googleClientId = "google-client-id",
            ).validate()
        }
    }

    @Test
    fun validateFailsWhenAnonKeyIsBlank() {
        assertFailsWith<IllegalArgumentException> {
            SupabaseSettings(
                url = "https://example.supabase.co",
                anonKey = "",
                googleClientId = "google-client-id",
            ).validate()
        }
    }
}
