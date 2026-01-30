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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExperimentalGetImage
class FaceRegistrationActivity : AppCompatActivity() {

    private val faceEmbeddingExtractor by lazy { FaceEmbeddingExtractor(this) }
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null

    private lateinit var previewView: PreviewView
    private lateinit var faceOverlay: View

    @Volatile
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_registration)

        previewView = findViewById(R.id.previewView)
        faceOverlay = findViewById(R.id.faceOverlay)

        cameraExecutor = Executors.newSingleThreadExecutor()
        requestCameraPermission()
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            cameraProvider = providerFuture.get()
            bindUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindUseCases() {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, analyzerCallback)
            }

        provider.bindToLifecycle(
            this,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            preview,
            analyzer
        )
    }

    private val analyzerCallback = ImageAnalysis.Analyzer { imageProxy ->
        if (isProcessing) {
            imageProxy.close()
            return@Analyzer
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return@Analyzer
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        FaceDetection.getClient(
            FaceDetectorOptions.Builder().build()
        ).process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    isProcessing = true
                    processFace(faces.first(), imageProxy)
                } else {
                    runOnUiThread {
                        faceOverlay.setBackgroundResource(R.drawable.oval_border_red)
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun processFace(face: Face, imageProxy: ImageProxy) {
        runOnUiThread {
            faceOverlay.setBackgroundResource(R.drawable.oval_border_green)
        }

        val bitmap = cropFace(imageProxy, face.boundingBox)
        val embedding = faceEmbeddingExtractor.getFaceEmbedding(bitmap)

        if (embedding != null) {
            saveOwnerEmbedding(embedding)
            onSuccess()
        } else {
            onFailure()
        }
    }

    private fun saveOwnerEmbedding(embedding: FloatArray) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val value = embedding.joinToString(",")
        prefs.edit()
            .putString("OWNER_FACE_EMBEDDING", value)
            .apply()
    }

    private fun onSuccess() {
        runOnUiThread {
            Toast.makeText(this, "Face registered successfully", Toast.LENGTH_SHORT).show()
            startActivity(
                Intent(this, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
        }
    }

    private fun onFailure() {
        runOnUiThread {
            Toast.makeText(this, "Face registration failed. Try again.", Toast.LENGTH_LONG).show()
            isProcessing = false
            faceOverlay.setBackgroundResource(R.drawable.oval_border_red)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun cropFace(imageProxy: ImageProxy, box: Rect): Bitmap {
        val fullBitmap = imageProxy.image!!.toBitmap()
        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
        }

        val rotated = Bitmap.createBitmap(
            fullBitmap, 0, 0,
            fullBitmap.width, fullBitmap.height,
            matrix, true
        )

        val left = box.left.coerceAtLeast(0)
        val top = box.top.coerceAtLeast(0)
        val width = box.width().coerceAtMost(rotated.width - left)
        val height = box.height().coerceAtMost(rotated.height - top)

        return Bitmap.createBitmap(rotated, left, top, width, height)
    }

    private fun Image.toBitmap(): Bitmap {
        val y = planes[0].buffer
        val u = planes[1].buffer
        val v = planes[2].buffer

        val nv21 = ByteArray(y.remaining() + u.remaining() + v.remaining())
        y.get(nv21, 0, y.remaining())
        v.get(nv21, y.remaining(), v.remaining())
        u.get(nv21, y.remaining() + v.remaining(), u.remaining())

        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, width, height), 100, out)
        return BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        faceEmbeddingExtractor.close()
        super.onDestroy()
    }
}
