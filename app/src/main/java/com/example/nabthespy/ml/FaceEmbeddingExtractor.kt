// In: com.example.nabthespy.ml.FaceEmbeddingExtractor.kt

package com.example.nabthespy.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.sqrt
import org.tensorflow.lite.Interpreter

class FaceEmbeddingExtractor(context: Context) {

    private var interpreter: Interpreter?
    private var inputSize: Int
    private var embeddingSize: Int

    companion object {
        private const val TAG = "FaceEmbeddingExtractor"
        private const val MODEL_NAME = "mobile_facenet.tflite"
    }

    init {
        try {
            val modelBuffer = loadModelFile(context, MODEL_NAME)
            val options = Interpreter.Options()
            // To improve performance on supported devices, you could add a GPU delegate
            // options.addDelegate(GpuDelegate())
            interpreter = Interpreter(modelBuffer, options)

            val inputTensor = interpreter!!.getInputTensor(0)
            val outputTensor = interpreter!!.getOutputTensor(0)
            inputSize = inputTensor.shape()[1]
            embeddingSize = outputTensor.shape()[1]

            Log.d(TAG, "Model loaded successfully. Input size: $inputSize, Embedding size: $embeddingSize")
        } catch (e: IOException) {
            // This handles errors if the model file is missing, preventing a crash.
            Log.e(TAG, "Error loading model file: $MODEL_NAME", e)
            interpreter = null
            inputSize = 0
            embeddingSize = 0
        }
    }

    /**
     * Loads the TFLite model from the assets folder.
     */
    private fun loadModelFile(context: Context, modelName: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelName)
        return FileInputStream(assetFileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    /**
     * Takes a Bitmap of a face and returns its embedding.
     */
    fun getFaceEmbedding(faceBitmap: Bitmap): FloatArray? {
        // Check if the interpreter was loaded successfully.
        if (interpreter == null) {
            Log.e(TAG, "Interpreter is not initialized.")
            return null
        }

        val preprocessedBitmap = Bitmap.createScaledBitmap(faceBitmap, inputSize, inputSize, true)
        val inputBuffer = convertBitmapToByteBuffer(preprocessedBitmap)
        val outputEmbedding = Array(1) { FloatArray(embeddingSize) }

        interpreter?.run(inputBuffer, outputEmbedding)

        return l2Normalize(outputEmbedding[0])
    }

    /**
     * Converts a Bitmap into a ByteBuffer for the TFLite model.
     * Normalizes pixel values from [0, 255] to [-1, 1].
     */
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val value = intValues[pixel++]
                // This formula converts each color channel (R, G, B) from
                // the range [0, 255] to the range [-1.0, 1.0], which the model expects.
                byteBuffer.putFloat(((value shr 16 and 0xFF) - 127.5f) / 127.5f) // Red
                byteBuffer.putFloat(((value shr 8 and 0xFF) - 127.5f) / 127.5f)  // Green
                byteBuffer.putFloat(((value and 0xFF) - 127.5f) / 127.5f)       // Blue
            }
        }
        return byteBuffer
    }

    /**
     * Normalizes a vector to have a unit length (L2 norm of 1).
     */
    private fun l2Normalize(embedding: FloatArray): FloatArray {
        var norm = 0.0f
        for (value in embedding) {
            norm += value * value
        }
        norm = sqrt(norm)

        if (norm > 0) {
            for (i in embedding.indices) {
                embedding[i] = embedding[i] / norm
            }
        }
        return embedding
    }

    /**
     * A method to release TFLite resources.
     */
    fun close() {
        interpreter?.close()
    }
}