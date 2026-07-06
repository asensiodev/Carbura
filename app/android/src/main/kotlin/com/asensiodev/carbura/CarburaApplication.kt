package com.asensiodev.carbura

import android.app.Application
import com.asensiodev.carbura.app.shared.initKoin
import com.asensiodev.carbura.core.auth.SupabaseSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class CarburaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@CarburaApplication)
            modules(
                module {
                    single {
                        SupabaseSettings(
                            url = BuildConfig.SUPABASE_URL,
                            anonKey = BuildConfig.SUPABASE_ANON_KEY,
                            googleClientId = BuildConfig.GOOGLE_CLIENT_ID,
                        )
                    }
                },
            )
        }
    }
}
