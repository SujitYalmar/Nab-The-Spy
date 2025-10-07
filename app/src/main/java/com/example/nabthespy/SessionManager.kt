package com.example.nabthespy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor

    private val PRIVATE_MODE = 0

    companion object {
        const val IS_LOGGED_IN = "isLoggedIn"
        private const val SESSIONS_DIR = "sessions"
        const val KEY_USER_PIN = "user_pin"

        // ADD THIS KEY for the new feature
        private const val WATCH_MODE_ENABLED = "watchModeEnabled"
    }

    init {
        editor = prefs.edit()
    }

    fun createLoginSession(pin: String) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(KEY_USER_PIN, pin)
        editor.commit()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user[KEY_USER_PIN] = prefs.getString(KEY_USER_PIN, null)
        return user
    }

    fun logoutUser() {
        editor.clear()
        editor.commit()

        val intent = Intent(context, KeyLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    // --- Your existing functions for handling session recordings remain unchanged ---

    fun createNewSessionDirectory(): File? {
        val sessionsDir = File(context.filesDir, SESSIONS_DIR)
        if (!sessionsDir.exists()) {
            sessionsDir.mkdir()
        }
        val newSessionDir = File(sessionsDir, System.currentTimeMillis().toString())
        return if (newSessionDir.mkdir()) newSessionDir else null
    }

    fun saveSnapshotToSession(sessionDir: File, tempSnapshotPath: String) {
        val sourceFile = File(tempSnapshotPath)
        val destinationFile = File(sessionDir, "snapshot.jpg")
        if (sourceFile.exists()) {
            sourceFile.renameTo(destinationFile)
        }
    }

    fun saveScreenCapture(sessionDir: File, bitmap: Bitmap, captureIndex: Int) {
        val file = File(sessionDir, "capture_$captureIndex.jpg")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getAllSessions(): List<RecordedSession> {
        val sessions = mutableListOf<RecordedSession>()
        val sessionsDir = File(context.filesDir, SESSIONS_DIR)

        if (!sessionsDir.exists() || !sessionsDir.isDirectory) {
            return emptyList()
        }

        sessionsDir.listFiles()?.forEach { sessionDir ->
            if (sessionDir.isDirectory) {
                val snapshotFile = File(sessionDir, "snapshot.jpg")
                if (snapshotFile.exists()) {
                    try {
                        val timestampMillis = sessionDir.name.toLong()
                        val formattedTimestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            .format(Date(timestampMillis))
                        sessions.add(RecordedSession(snapshotFile.absolutePath, formattedTimestamp))
                    } catch (e: NumberFormatException) { /* Ignore non-timestamp folders */ }
                }
            }
        }
        return sessions.sortedByDescending { session ->
            File(session.snapshotPath).parentFile?.name?.toLongOrNull() ?: 0
        }
    }

    // --- ADD THESE NEW FUNCTIONS for Watch Mode ---
    fun setWatchModeEnabled(isEnabled: Boolean) {
        editor.putBoolean(WATCH_MODE_ENABLED, isEnabled).apply()
    }

    fun isWatchModeEnabled(): Boolean {
        return prefs.getBoolean(WATCH_MODE_ENABLED, false)
    }
}