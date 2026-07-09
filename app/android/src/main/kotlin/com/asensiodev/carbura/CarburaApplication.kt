package com.asensiodev.carbura

import android.app.Application
import com.asensiodev.carbura.core.auth.authModule
import com.asensiodev.carbura.core.auth.SupabaseSettings
import com.asensiodev.carbura.core.data.dataModule
import com.asensiodev.carbura.feature.garage.di.garageModule
import com.asensiodev.carbura.feature.maintenance.di.maintenanceModule
import com.asensiodev.carbura.feature.onboarding.di.onboardingModule
import com.asensiodev.carbura.feature.reminders.di.remindersModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class CarburaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CarburaApplication)
            modules(
                authModule,
                dataModule,
                onboardingModule,
                garageModule,
                maintenanceModule,
                remindersModule,
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
