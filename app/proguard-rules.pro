# R8 / ProGuard Keep Rules for Focus Shield

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.Dao *;
}
-dontwarn androidx.room.paging.**

# Firebase Analytics & Messaging
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.**

# Google Mobile Ads (AdMob)
-keep class com.google.android.gms.ads.nativead.NativeAdView { *; }
-dontwarn com.google.android.gms.ads.**

# DataStore & Kotlin Coroutines
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences$Key { *; }
-dontwarn kotlinx.coroutines.**

# Keep Enum values for Room & Serialization
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
