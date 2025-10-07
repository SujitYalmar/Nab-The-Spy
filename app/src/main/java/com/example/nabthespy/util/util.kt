package com.example.nabthespy.util

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer: ByteBuffer = planes[0].buffer
    val uBuffer: ByteBuffer = planes[1].buffer
    val vBuffer: ByteBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // U and V are swapped
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 90, out)
    val imageBytes = out.toByteArray()
    return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
