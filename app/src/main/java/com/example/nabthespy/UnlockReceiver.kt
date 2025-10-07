package com.example.nabthespy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class UnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // We only care about the user unlocking the phone
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            // ADD THIS LINE for precise debugging
            Log.d("NabTheSpy_DEBUG", "UNLOCK DETECTED! The receiver is working.")

            val sessionManager = SessionManager(context)

            // Check if the user has enabled this feature in the app
            if (sessionManager.isWatchModeEnabled()) {
                // ADD THIS LINE for precise debugging
                Log.d("NabTheSpy_DEBUG", "Watch Mode is ON. Attempting to start service...")

                val serviceIntent = Intent(context, WatchModeService::class.java)

                // Use startForegroundService for modern Android versions to avoid crashes
                ContextCompat.startForegroundService(context, serviceIntent)
            } else {
                // ADD THIS LINE for precise debugging
                Log.d("NabTheSpy_DEBUG", "Watch Mode is OFF. Not starting service.")
            }
        }
    }
}

