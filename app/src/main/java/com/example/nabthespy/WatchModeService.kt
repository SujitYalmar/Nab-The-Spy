package com.example.nabthespy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.nabthespy.util.FaceRecognizer
import com.example.nabthespy.util.SecureStorageHelper
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import com.example.nabthespy.util.MediaProjectionManager as AppMediaProjectionManager

class WatchModeService : LifecycleService() {

    private lateinit var faceRecognizer: FaceRecognizer

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null

    override fun onCreate() {
        super.onCreate()
        faceRecognizer = FaceRecognizer(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🚨 WatchModeService started")
        takeSelfieAndVerify()
        return START_NOT_STICKY
    }

    /* ---------------------------------------------------------
     * 📸 CAMERA + FACE VERIFICATION
     * --------------------------------------------------------- */
    private fun takeSelfieAndVerify() {
        val providerFuture = ProcessCameraProvider.getInstance(this)

        providerFuture.addListener({
            try {
                val cameraProvider = providerFuture.get()

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider { request ->
                    val surface = Surface(SurfaceTexture(0))
                    request.provideSurface(surface, executor) {}
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageCapture
                )

                imageCapture.takePicture(
                    executor,
                    object : ImageCapture.OnImageCapturedCallback() {

                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = image.toBitmap()
                            image.close()

                            if (bitmap != null) {
                                verifyFace(bitmap)
                            } else {
                                Log.e(TAG, "Bitmap null → intruder assumed")
                                startScreenCapture()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e(TAG, "Camera error → intruder assumed", exception)
                            startScreenCapture()
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera init failed", e)
                startScreenCapture()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun verifyFace(bitmap: Bitmap) {
        val savedEmbedding = SecureStorageHelper.getMasterEmbedding(this)

        if (savedEmbedding == null) {
            Log.e(TAG, "No saved face → intruder")
            startScreenCapture()
            return
        }

        faceRecognizer.getFaceEmbedding(bitmap) { current ->
            if (current == null) {
                Log.e(TAG, "Face not detected → intruder")
                startScreenCapture()
                return@getFaceEmbedding
            }

            val distance = faceRecognizer.calculateDistance(savedEmbedding, current)
            Log.d(TAG, "Face distance = $distance")

            if (distance <= FaceRecognizer.EMBEDDING_DISTANCE_THRESHOLD) {
                Log.d(TAG, "✅ Owner verified → stopping")
                stopSelf()
            } else {
                Log.d(TAG, "❌ Intruder detected → screen capture")
                startScreenCapture()
            }
        }
    }

    /* ---------------------------------------------------------
     * 🖥 MEDIA PROJECTION (SCREEN CAPTURE)
     * --------------------------------------------------------- */
    private fun startScreenCapture() {
        if (mediaProjection != null) return

        val resultCode = AppMediaProjectionManager.resultCode
        val permissionIntent = AppMediaProjectionManager.projectionIntent

        if (permissionIntent == null) {
            Log.e(TAG, "❌ MediaProjection permission missing")
            stopSelf()
            return
        }

        val manager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        mediaProjection = manager.getMediaProjection(resultCode, permissionIntent)

        val metrics = resources.displayMetrics

        imageReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            PixelFormat.RGBA_8888,
            2
        )

        mediaProjection?.createVirtualDisplay(
            "NabTheSpyScreen",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface,
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

        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )

        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        val cropped = Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)

        val file = File(filesDir, "screen_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use {
            cropped.compress(Bitmap.CompressFormat.JPEG, 85, it)
        }

        Log.d(TAG, "📸 Screen saved: ${file.absolutePath}")
    }

    override fun onDestroy() {
        handler.removeCallbacks(screenRunnable)
        imageReader?.close()
        mediaProjection?.stop()
        executor.shutdown()
        super.onDestroy()
    }

    /* ---------------------------------------------------------
     * 🔔 NOTIFICATION
     * --------------------------------------------------------- */
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
            .setContentText("Intruder monitoring in progress")
            .setSmallIcon(R.drawable.ic_security_shield)
            .setOngoing(true)
            .build()

    companion object {
        private const val TAG = "WatchModeService"
        private const val CHANNEL_ID = "watch_mode_channel"
        private const val NOTIFICATION_ID = 201
    }
}

/* ---------------------------------------------------------
 * 🔧 ImageProxy → Bitmap
 * --------------------------------------------------------- */
private fun ImageProxy.toBitmap(): Bitmap? {
    val buffer = planes.firstOrNull()?.buffer ?: return null
    buffer.rewind()
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
