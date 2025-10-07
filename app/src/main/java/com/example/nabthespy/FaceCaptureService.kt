package com.example.nabthespy

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.nabthespy.ml.FaceEmbeddingExtractor
import com.example.nabthespy.ml.FaceMatcher
import com.example.nabthespy.util.SecureStorageHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors

class FaceCaptureService : Service() {

    private lateinit var faceEmbeddingExtractor: FaceEmbeddingExtractor
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate() {
        super.onCreate()
        faceEmbeddingExtractor = FaceEmbeddingExtractor(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_PROCESS_IMAGE) {
            val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
            if (imagePath != null) {
                executor.execute { processCapturedImage(imagePath) }
            } else {
                stopSelf()
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                val cameraIntent = Intent(this, CameraCaptureActivity::class.java)
                cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(cameraIntent)
            }, 3000)
        }
        return START_NOT_STICKY
    }

    private fun processCapturedImage(imagePath: String) {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val image = InputImage.fromBitmap(bitmap, 0)
        val detector = FaceDetection.getClient(FaceDetectorOptions.Builder().build())

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    // ## THIS LINE IS NOW CORRECTED ##
                    val faceBitmap = cropFace(bitmap, face.boundingBox)
                    val newEmbedding = faceEmbeddingExtractor.getFaceEmbedding(faceBitmap)

                    if (newEmbedding != null) {
                        val ownerEmbedding = SecureStorageHelper.getMasterEmbedding(this)
                        if (ownerEmbedding == null) {
                            Log.e("NabTheSpy", "Owner embedding not found.")
                            finishProcessing(imagePath)
                            return@addOnSuccessListener
                        }

                        val isMatch = FaceMatcher.areFacesSame(ownerEmbedding, newEmbedding)
                        if (isMatch) {
                            Log.d("NabTheSpy", "Face matched. Owner verified.")
                        } else {
                            Log.d("NabTheSpy", "Face mismatch! Intruder detected.")
                            triggerWatchMode(bitmap)
                        }
                    }
                } else {
                    Log.d("NabTheSpy", "No face detected in the captured image.")
                }
                finishProcessing(imagePath)
            }
            .addOnFailureListener { e ->
                Log.e("NabTheSpy", "Face detection failed.", e)
                finishProcessing(imagePath)
            }
    }

    private fun triggerWatchMode(intruderBitmap: Bitmap) {
        val tempFile = File(cacheDir, "intruder_snapshot.jpg")
        try {
            FileOutputStream(tempFile).use { out ->
                intruderBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            val watchModeIntent = Intent(this, WatchModeService::class.java).apply {
                putExtra("SNAPSHOT_PATH", tempFile.absolutePath)
            }
            startService(watchModeIntent)
        } catch (e: IOException) {
            Log.e("NabTheSpy", "Failed to save intruder snapshot", e)
        }
    }

    private fun cropFace(bitmap: Bitmap, boundingBox: Rect): Bitmap {
        return Bitmap.createBitmap(bitmap, boundingBox.left, boundingBox.top, boundingBox.width(), boundingBox.height())
    }

    private fun finishProcessing(imagePath: String) {
        File(imagePath).delete()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
        faceEmbeddingExtractor.close()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_PROCESS_IMAGE = "com.example.nabthespy.PROCESS_IMAGE"
        const val EXTRA_IMAGE_PATH = "image_path"
    }
}