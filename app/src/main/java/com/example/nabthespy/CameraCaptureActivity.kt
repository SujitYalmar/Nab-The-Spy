package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// This activity has no UI and is transparent. Its only job is to take a picture.
class CameraCaptureActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCameraAndCapture()
    }

    private fun startCameraAndCapture() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder().build()

            try {
                // Bind the camera use case
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, imageCapture)
                // Once bound, immediately take the photo
                takePhoto()
            } catch (exc: Exception) {
                Log.e("NabTheSpy", "Camera binding failed", exc)
                finish() // Close if camera fails
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create a file to save the image
        val photoFile = File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Photo was saved successfully.
                    // Now, start the service again to process the saved image.
                    val serviceIntent = Intent(applicationContext, FaceCaptureService::class.java).apply {
                        action = FaceCaptureService.ACTION_PROCESS_IMAGE
                        putExtra(FaceCaptureService.EXTRA_IMAGE_PATH, photoFile.absolutePath)
                    }
                    startService(serviceIntent)
                    finish() // Close this invisible activity
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("NabTheSpy", "Photo capture failed: ${exc.message}", exc)
                    finish() // Close this invisible activity
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}