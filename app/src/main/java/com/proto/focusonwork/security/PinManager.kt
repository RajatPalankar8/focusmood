package com.proto.focusonwork.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

class PinManager(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun hasPin(): Boolean = preferences.contains(KEY_HASH)

    fun savePin(pin: String) {
        require(pin.length == 4 && pin.all(Char::isDigit)) { "PIN must contain four digits" }
        val salt = ByteArray(SALT_SIZE).also(SecureRandom()::nextBytes)
        preferences.edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, hash(pin, salt))
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val encodedSalt = preferences.getString(KEY_SALT, null) ?: return false
        val expected = preferences.getString(KEY_HASH, null) ?: return false
        val salt = Base64.decode(encodedSalt, Base64.NO_WRAP)
        return MessageDigest.isEqual(hash(pin, salt).toByteArray(), expected.toByteArray())
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return Base64.encodeToString(digest.digest(salt + pin.toByteArray()), Base64.NO_WRAP)
    }

    companion object {
        private const val PREFERENCES_NAME = "secure_focus_preferences"
        private const val KEY_SALT = "pin_salt"
        private const val KEY_HASH = "pin_hash"
        private const val SALT_SIZE = 16
    }
}
