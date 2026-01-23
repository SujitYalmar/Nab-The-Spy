package com.example.nabthespy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.nabthespy.util.MediaProjectionManager as AppMediaProjectionManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.view.Surface
import android.graphics.SurfaceTexture


class WatchModeService : LifecycleService() {

    private lateinit var sessionManager: SessionManager
    private lateinit var cameraExecutor: ExecutorService

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null

    private val handler = Handler(Looper.getMainLooper())
    private var screenshotCount = 0

    private lateinit var snapshotFile: File

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WatchModeService started")
        takeSelfie()
        return START_NOT_STICKY
    }

    /**
     * 📸 Take intruder selfie
     */
    private fun takeSelfie() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider { request ->
                    request.provideSurface(
                        Surface(SurfaceTexture(0)),
                        cameraExecutor
                    ) {}
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageCapture
                )

                imageCapture.takePicture(
                    cameraExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = image.toBitmap()
                            image.close()

                            if (bitmap != null) {
                                saveSelfie(bitmap)
                                saveSessionEntry()
                                startScreenCapture()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e(TAG, "Selfie capture failed", exception)
                            startScreenCapture()
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera error", e)
                startScreenCapture()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 💾 Save selfie file
     */
    private fun saveSelfie(bitmap: Bitmap) {
        snapshotFile = File(filesDir, "intruder_${System.currentTimeMillis()}.jpg")
        FileOutputStream(snapshotFile).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
    }

    /**
     * 🕒 Save timeline entry
     */
    private fun saveSessionEntry() {
        SessionManager.addSession(
            this,
            Session(
                snapshotPath = snapshotFile.absolutePath,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * 🖥 Screen capture loop
     */
    private fun startScreenCapture() {
        val resultCode = AppMediaProjectionManager.resultCode
        val intent = AppMediaProjectionManager.projectionIntent ?: return

        val manager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        mediaProjection = manager.getMediaProjection(resultCode, intent)

        val metrics = resources.displayMetrics
        imageReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            PixelFormat.RGBA_8888,
            2
        )

        mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            handler
        )

        handler.post(screenRunnable)
    }

    private val screenRunnable = object : Runnable {
        override fun run() {
            captureScreen()
            handler.postDelayed(this, 10_000)
        }
    }

    private fun captureScreen() {
        val image = imageReader?.acquireLatestImage() ?: return
        val buffer = image.planes[0].buffer

        val bitmap = Bitmap.createBitmap(
            image.width,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        val file = File(filesDir, "screen_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
        }

        screenshotCount++
    }

    override fun onDestroy() {
        handler.removeCallbacks(screenRunnable)
        mediaProjection?.stop()
        imageReader?.close()
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Watch Mode",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NabTheSpy Active")
            .setContentText("Monitoring intruder activity")
            .setSmallIcon(R.drawable.ic_security_shield)
            .setOngoing(true)
            .build()

    companion object {
        private const val TAG = "WatchModeService"
        private const val CHANNEL_ID = "watch_mode_channel"
        private const val NOTIFICATION_ID = 101
    }
}

/**
 * 🔧 ImageProxy → Bitmap
 */
private fun ImageProxy.toBitmap(): Bitmap? {
    val buffer = planes.firstOrNull()?.buffer ?: return null
    buffer.rewind()
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
