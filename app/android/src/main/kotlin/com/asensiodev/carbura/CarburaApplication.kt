package com.asensiodev.carbura

import android.app.Application
import com.asensiodev.carbura.app.shared.initKoin
import org.koin.android.ext.koin.androidContext

class CarburaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@CarburaApplication)
        }
    }
}
