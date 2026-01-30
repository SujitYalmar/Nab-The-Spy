package com.example.nabthespy

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SecurityAdminReceiver : DeviceAdminReceiver() {

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        Log.d("SecurityAdmin", "❌ Wrong password detected")

        // 1️⃣ Start selfie capture safely (foreground service)
        val faceIntent = Intent(context, FaceCaptureService::class.java).apply {
            putExtra("CAPTURE_REASON", "PASSWORD_FAILED")
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(faceIntent)
        } else {
            context.startService(faceIntent)
        }

        // 2️⃣ Notify DetectionService (for logging / backend sync)
        val detectionIntent = Intent(context, DetectionService::class.java).apply {
            putExtra("EVENT_SOURCE", "PASSWORD_FAILED")
        }

        context.startService(detectionIntent)
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        super.onPasswordSucceeded(context, intent)
        Log.d("SecurityAdmin", "✅ Password success (no action taken)")
    }
}
