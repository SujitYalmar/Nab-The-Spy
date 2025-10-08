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

// ✅ Extends LifecycleService for CameraX compatibility
class WatchModeService : LifecycleService() {

    private lateinit var sessionManager: SessionManager
    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private lateinit var sessionDir: File
    private lateinit var cameraExecutor: ExecutorService

    private val handler = Handler(Looper.getMainLooper())
    private var screenshotCount = 0

    private val screenshotRunnable = object : Runnable {
        override fun run() {
            captureScreen()
            // Capture every 10 seconds
            handler.postDelayed(this, 10000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("NabTheSpy_DEBUG", "SUCCESS! WatchModeService has started.")

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        sessionManager.createNewSessionDirectory()?.let {
            sessionDir = it
            Log.d(TAG, "New session created. Taking selfie...")
            takeSelfie()
        } ?: run {
            Log.e(TAG, "Failed to create session directory.")
            stopSelf()
        }

        return START_NOT_STICKY
    }

    /**
     * Take a selfie using CameraX front camera
     */
    private fun takeSelfie() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                // Dummy preview (CameraX requires a surface)
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider { request ->
                    request.provideSurface(
                        android.view.Surface(SurfaceTexture(0)),
                        cameraExecutor
                    ) { }
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

                imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        Log.d(TAG, "Selfie captured successfully.")
                        val bitmap = image.toBitmap()
                        if (bitmap != null) {
                            saveSelfie(bitmap)
                            Log.d(TAG, "Saved selfie. Bitmap size: ${bitmap.width}x${bitmap.height}")
                        } else {
                            Log.e(TAG, "Failed to convert ImageProxy to Bitmap")
                        }
                        image.close()
                        startScreenshotLoop()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Selfie capture failed: ${exception.message}", exception)
                        startScreenshotLoop()
                    }
                })
            } catch (exc: Exception) {
                Log.e(TAG, "CameraX binding failed", exc)
                startScreenshotLoop()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * ✅ Fixed: Proper MediaProjection setup for Android 14/15 and Samsung devices
     */
    private fun startScreenshotLoop() {
        Log.d(TAG, "Initializing screen capture.")
        val resultCode = AppMediaProjectionManager.resultCode
        val projectionIntent = AppMediaProjectionManager.projectionIntent

        if (resultCode != 0 && projectionIntent != null) {
            val mediaProjectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, projectionIntent)

            val projection = mediaProjection ?: run {
                Log.e(TAG, "MediaProjection is null.")
                stopSelf()
                return
            }

            val metrics = resources.displayMetrics
            imageReader = ImageReader.newInstance(
                metrics.widthPixels,
                metrics.heightPixels,
                PixelFormat.RGBA_8888,
                2
            )

            // ✅ Register callback BEFORE creating VirtualDisplay (Android 14+ requirement)
            projection.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    Log.d(TAG, "MediaProjection stopped.")
                    imageReader?.close()
                    handler.removeCallbacks(screenshotRunnable)
                }
            }, handler)

            try {
                projection.createVirtualDisplay(
                    "ScreenCapture",
                    metrics.widthPixels,
                    metrics.heightPixels,
                    metrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface,
                    null,
                    handler
                )
                Log.d(TAG, "Virtual display created successfully. Starting screenshot loop.")
                handler.post(screenshotRunnable)

            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to create virtual display: ${e.message}", e)
                stopSelf()
            }
        } else {
            Log.e(TAG, "MediaProjection permission is missing. Stopping service.")
            stopSelf()
        }
    }

    /**
     * Capture screen frame from ImageReader
     */
    private fun captureScreen() {
        val image = imageReader?.acquireLatestImage() ?: return
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        sessionManager.saveScreenCapture(sessionDir, bitmap, screenshotCount)
        Log.d(TAG, "Saved screen capture #$screenshotCount")
        screenshotCount++
    }

    private fun saveSelfie(bitmap: Bitmap) {
        val destinationFile = File(sessionDir, "snapshot.jpg")
        try {
            FileOutputStream(destinationFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save selfie.", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(screenshotRunnable)
        mediaProjection?.stop()
        imageReader?.close()
        cameraExecutor.shutdown()
        Log.d(TAG, "WatchModeService destroyed.")
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Watch Mode Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NabTheSpy is Active")
            .setContentText("Monitoring for unauthorized access.")
            .setSmallIcon(R.drawable.ic_security_shield)
            .setOngoing(true) // Keeps service alive
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    companion object {
        private const val TAG = "NabTheSpy_Service"
        const val CHANNEL_ID = "WatchModeServiceChannel"
        const val NOTIFICATION_ID = 1
    }
}

/**
 * Extension to convert ImageProxy to Bitmap
 */
private fun ImageProxy.toBitmap(): Bitmap? {
    val planeProxy = planes.firstOrNull() ?: return null
    val buffer = planeProxy.buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
