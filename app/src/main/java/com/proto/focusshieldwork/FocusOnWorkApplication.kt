package com.proto.focusshieldwork

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging

class FocusOnWorkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.APP_OPEN, null)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FocusFCM", "FCM Registration Token: ${task.result}")
            }
        }
    }
}
