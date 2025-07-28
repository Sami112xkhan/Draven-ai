package com.samikhan.draven

import android.app.Application

class DravenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        lateinit var instance: DravenApplication
            private set
    }

    init {
        instance = this
    }
}
