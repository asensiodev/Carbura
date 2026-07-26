package com.asensiodev.carbura.core.auth

import com.asensiodev.carbura.core.domain.auth.AuthGateway
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.dsl.module

val authModule =
    module {
        single<SupabaseClient> {
            val settings = get<SupabaseSettings>()
            settings.validate()
            createSupabaseClient(
                supabaseUrl = settings.url,
                supabaseKey = settings.anonKey,
            ) {
                configurePlatformClient()
                install(Auth) {
                    configurePlatformAuth(settings)
                }
                install(Postgrest)
            }
        }
        single<AuthGateway> { createPlatformAuthGateway(get()) }
    }

internal expect fun AuthConfig.configurePlatformAuth(settings: SupabaseSettings)

internal expect fun SupabaseClientBuilder.configurePlatformClient()

internal expect fun createPlatformAuthGateway(client: SupabaseClient): AuthGateway
