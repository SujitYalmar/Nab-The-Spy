package com.example.nabthespy.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecureStorageHelper {

    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_PIN = "user_pin"

    // Get encrypted shared preferences instance
    private fun getPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            PREFS_NAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    // Save PIN securely
    fun savePin(context: Context, pin: String) {
        getPrefs(context).edit().putString(KEY_PIN, pin).apply()
    }

    // Retrieve saved PIN (null if not set yet)
    fun getPin(context: Context): String? {
        return getPrefs(context).getString(KEY_PIN, null)
    }

    // Check if PIN already exists
    fun isPinSet(context: Context): Boolean {
        return getPin(context) != null
    }

    // Clear stored PIN (optional, for reset logic)
    fun clearPin(context: Context) {
        getPrefs(context).edit().remove(KEY_PIN).apply()
    }
}
