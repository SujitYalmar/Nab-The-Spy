package com.example.nabthespy

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.nabthespy.ml.FaceEmbeddingExtractor
import com.example.nabthespy.util.SecureStorageHelper
import com.example.nabthespy.HomeActivity // Correct HomeActivity import
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExperimentalGetImage
class FaceRegistrationActivity : AppCompatActivity() {

    private val faceEmbeddingExtractor: FaceEmbeddingExtractor by lazy {
        FaceEmbeddingExtractor(this)
    }
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    private lateinit var previewView: PreviewView
    private lateinit var faceOverlay: View

    @Volatile
    private var isFaceRegistrationInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_registration)

        previewView = findViewById(R.id.previewView)
        faceOverlay = findViewById(R.id.faceOverlay)
        cameraExecutor = Executors.newSingleThreadExecutor()

        requestCameraPermission()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCameraAndAnalyzer()
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraAndAnalyzer()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCameraAndAnalyzer() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, faceAnalyzer)
            }
        try {
            provider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        } catch (e: Exception) {
            Log.e("FaceRegistration", "Use case binding failed", e)
        }
    }

    private val faceAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        if (isFaceRegistrationInProgress) {
            imageProxy.close()
            return@Analyzer
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val detector = FaceDetection.getClient(FaceDetectorOptions.Builder().build())

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        isFaceRegistrationInProgress = true
                        processFace(faces[0], imageProxy)
                    } else {
                        runOnUiThread { faceOverlay.setBackgroundResource(R.drawable.oval_border_red) }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FaceRegistration", "Face detection failed.", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun processFace(face: Face, imageProxy: ImageProxy) {
        runOnUiThread { faceOverlay.setBackgroundResource(R.drawable.oval_border_green) }

        val faceBitmap = cropFaceFromImage(imageProxy, face.boundingBox)
        val embedding = faceEmbeddingExtractor.getFaceEmbedding(faceBitmap)

        if (embedding != null) {
            SecureStorageHelper.saveMasterEmbedding(this, embedding)
            onRegistrationSuccess()
        } else {
            onRegistrationFailure()
        }
    }

    private fun onRegistrationSuccess() {
        runOnUiThread {
            Toast.makeText(this, "Face registered successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun onRegistrationFailure() {
        runOnUiThread {
            Toast.makeText(this, "Could not register face. Please try again.", Toast.LENGTH_LONG).show()
            isFaceRegistrationInProgress = false
            faceOverlay.setBackgroundResource(R.drawable.oval_border_red)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun cropFaceFromImage(imageProxy: ImageProxy, boundingBox: Rect): Bitmap {
        val fullBitmap = imageProxy.image!!.toBitmap()
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotatedBitmap = Bitmap.createBitmap(fullBitmap, 0, 0, fullBitmap.width, fullBitmap.height, matrix, true)

        val left = maxOf(0, boundingBox.left)
        val top = maxOf(0, boundingBox.top)
        val width = minOf(rotatedBitmap.width - left, boundingBox.width())
        val height = minOf(rotatedBitmap.height - top, boundingBox.height())
        return Bitmap.createBitmap(rotatedBitmap, left, top, width, height)
    }

    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer; val uBuffer = planes[1].buffer; val vBuffer = planes[2].buffer
        val ySize = yBuffer.remaining(); val uSize = uBuffer.remaining(); val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize); vBuffer.get(nv21, ySize, vSize); uBuffer.get(nv21, ySize + vSize, uSize)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        faceEmbeddingExtractor.close()
    }
}