package com.example.nabthespy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService

class DetectionService : LifecycleService() {

    companion object {
        const val CHANNEL_ID = "detection_service_channel"
        const val NOTIFICATION_ID = 101
        const val EXTRA_EVENT_SOURCE = "EVENT_SOURCE"
        const val EVENT_UNLOCK_SUCCESS = "UNLOCK_SUCCESS"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("DetectionService", "✅ DetectionService created")

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NabTheSpy Active")
            .setContentText("Monitoring security events")
            .setSmallIcon(R.drawable.ic_security_shield)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val source = intent?.getStringExtra(EXTRA_EVENT_SOURCE)
        Log.d("DetectionService", "🔔 Triggered by: $source")

        if (source == EVENT_UNLOCK_SUCCESS) {
            startWatchMode()
        }

        // Stop immediately after dispatching
        stopSelf()
        return Service.START_NOT_STICKY
    }

    private fun startWatchMode() {
        Log.d("DetectionService", "🚀 Starting WatchModeService")

        val watchIntent = Intent(this, WatchModeService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(watchIntent)
        } else {
            startService(watchIntent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NabTheSpy Detection",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
