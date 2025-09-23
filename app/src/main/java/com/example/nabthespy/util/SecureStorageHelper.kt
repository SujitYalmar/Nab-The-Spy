package com.example.nabthespy.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecureStorageHelper {

    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_PIN = "user_pin"
    // ADDED: Key for storing the face embedding
    private const val KEY_EMBEDDING = "owner_embedding"

    // Get encrypted shared preferences instance
    private fun getPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            PREFS_NAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    // --- PIN Functions (Your existing code is kept) ---

    fun savePin(context: Context, pin: String) {
        getPrefs(context).edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(context: Context): String? {
        return getPrefs(context).getString(KEY_PIN, null)
    }

    fun isPinSet(context: Context): Boolean {
        return getPin(context) != null
    }

    fun clearPin(context: Context) {
        getPrefs(context).edit().remove(KEY_PIN).apply()
    }

    // --- ADDED: Face Embedding Functions ---

    /**
     * Saves the master face embedding as a comma-separated string.
     */
    fun saveMasterEmbedding(context: Context, embedding: FloatArray) {
        val embeddingString = embedding.joinToString(",")
        getPrefs(context).edit().putString(KEY_EMBEDDING, embeddingString).apply()
    }

    /**
     * Retrieves the master face embedding.
     * @return The FloatArray of the embedding, or null if not found.
     */
    fun getMasterEmbedding(context: Context): FloatArray? {
        val embeddingString = getPrefs(context).getString(KEY_EMBEDDING, null)
        // Convert the string back to a FloatArray
        return embeddingString?.split(',')
            ?.mapNotNull { it.toFloatOrNull() }
            ?.toFloatArray()
    }
}