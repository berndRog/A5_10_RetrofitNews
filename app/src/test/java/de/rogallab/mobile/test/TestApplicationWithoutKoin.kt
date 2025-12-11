package de.rogallab.mobile.test

import android.app.Application

class TestApplicationWithoutKoin : Application() {
    override fun onCreate() {
        super.onCreate()
        // we don't start koin
    }
}
