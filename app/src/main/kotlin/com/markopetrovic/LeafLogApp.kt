package com.markopetrovic.leaflog

import android.app.Application
import org.koin.core.context.startKoin
import com.markopetrovic.leaflog.di.appModule

class LeafLogApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            modules(appModule)
        }
    }
}
