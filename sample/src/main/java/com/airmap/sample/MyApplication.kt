package com.airmap.sample

import android.app.Application

import com.airmap.airmapsdk.networking.services.AirMap
import com.airmap.airmapsdk.util.AirMapConfig
import com.mapbox.mapboxsdk.Mapbox

import timber.log.Timber
import timber.log.Timber.DebugTree

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AirMap.init(this)
        Timber.plant(DebugTree())
        Mapbox.getInstance(this, AirMapConfig.getMapboxApiKey())
    }
}
