package com.blood.videochat

import android.app.Application

class App : Application() {

    companion object {
        lateinit var context: Application
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }

}