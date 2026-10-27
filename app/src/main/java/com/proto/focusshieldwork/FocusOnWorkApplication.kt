package com.proto.focusshieldwork

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics

class FocusOnWorkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
    }
}
