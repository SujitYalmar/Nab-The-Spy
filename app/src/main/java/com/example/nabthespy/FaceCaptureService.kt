package com.example.nabthespy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import java.io.File
import java.util.concurrent.Executors

class FaceCaptureService : LifecycleService() {

    private lateinit var imageCapture: ImageCapture
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        startCamera()
    }

    private fun startForegroundNotification() {
        val channelId = "nabdaspy_camera"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NabdaSpy Camera",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("NabdaSpy Security")
            .setContentText("Capturing intruder image")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(101, notification)
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)

        providerFuture.addListener({
            val provider = providerFuture.get()

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val selector = CameraSelector.DEFAULT_FRONT_CAMERA

            provider.unbindAll()
            provider.bindToLifecycle(this, selector, imageCapture)

            capture()

        }, ContextCompat.getMainExecutor(this))
    }

    private fun capture() {
        val file = File(cacheDir, "intruder_${System.currentTimeMillis()}.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            options,
            executor,
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("FaceCaptureService", "📸 Captured: ${file.absolutePath}")

                    // 🔥 SAVE SESSION HERE
                    SessionManager.addSession(
                        applicationContext,
                        Session(
                            snapshotPath = file.absolutePath,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    stopSelf()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("FaceCaptureService", "Capture failed", exception)
                    stopSelf()
                }
            }
        )
    }

    override fun onBind(intent: android.content.Intent): IBinder? {
        return super.onBind(intent)
    }
}
