package com.example.nabthespy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SessionManager(private val context: Context) {

    companion object {
        private const val PREF_NAME = "AppPrefs"
        private const val KEY_SESSIONS = "sessions"
        private const val IS_LOGGED_IN = "isLoggedIn"
        const val KEY_USER_PIN = "user_pin"   // 🔧 FIXED (public)
        private const val WATCH_MODE_ENABLED = "watchModeEnabled"

        fun addSession(context: Context, session: Session) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val existing = getSessions(context).toMutableList()
            existing.add(session)

            prefs.edit()
                .putString(KEY_SESSIONS, Gson().toJson(existing))
                .apply()
        }

        fun getSessions(context: Context): List<Session> {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()

            val type = object : TypeToken<List<Session>>() {}.type
            return Gson().fromJson(json, type)
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    fun createLoginSession(pin: String) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(KEY_USER_PIN, pin)
        editor.apply()
    }

    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user[KEY_USER_PIN] = prefs.getString(KEY_USER_PIN, null)
        return user
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun logoutUser() {
        editor.clear().apply()
        val intent = Intent(context, KeyLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun setWatchModeEnabled(enabled: Boolean) {
        editor.putBoolean(WATCH_MODE_ENABLED, enabled).apply()
    }

    fun isWatchModeEnabled(): Boolean {
        return prefs.getBoolean(WATCH_MODE_ENABLED, false)
    }
}
