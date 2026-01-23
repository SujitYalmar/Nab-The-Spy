// File: UnlockReceiver.kt
package com.example.nabthespy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class UnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        Log.d("UnlockReceiver", "Broadcast received: $action")

        when (action) {

            // ✅ Triggered AFTER successful unlock
            Intent.ACTION_USER_PRESENT -> {
                Log.d("UnlockReceiver", "User unlocked device")

                val serviceIntent = Intent(context, DetectionService::class.java).apply {
                    putExtra("EVENT_SOURCE", "UNLOCK_SUCCESS")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }

            // ✅ For manual testing
            "com.example.nabthespy.TEST_ACTION" -> {
                Log.d("UnlockReceiver", "Test action received")

                val testIntent = Intent(context, DetectionService::class.java).apply {
                    putExtra("EVENT_SOURCE", "TEST_TRIGGER")
                }

                context.startService(testIntent)
            }
        }
    }
}
