package com.asensiodev.carbura.core.auth

import com.asensiodev.carbura.core.domain.auth.AuthGateway
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.AuthConfig

internal actual fun AuthConfig.configurePlatformAuth(settings: SupabaseSettings) {
    require(settings.googleClientId.isNotBlank()) { "GOOGLE_CLIENT_ID is missing in local.properties" }
}

internal actual fun SupabaseClientBuilder.configurePlatformClient() = Unit

internal actual fun createPlatformAuthGateway(client: SupabaseClient): AuthGateway = SupabaseAuthGateway(client)
