package com.proto.focusonwork

import android.app.Application
import com.google.android.gms.ads.MobileAds

class FocusOnWorkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
    }
}
