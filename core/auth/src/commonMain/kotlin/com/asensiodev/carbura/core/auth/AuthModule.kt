package com.asensiodev.carbura.core.auth

import com.asensiodev.carbura.core.domain.AuthGateway
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.dsl.module

val authModule = module {
    single<SupabaseClient> {
        val settings = get<SupabaseSettings>()
        settings.validate()
        createSupabaseClient(
            supabaseUrl = settings.url,
            supabaseKey = settings.anonKey,
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
    single<AuthGateway> { SupabaseAuthGateway(get(), get<SupabaseSettings>().googleClientId) }
}
