package com.example.nabthespy.util

import android.app.Activity
import android.content.Intent

// This is a singleton object to hold the screen capture permission result
object MediaProjectionManager {
    var projectionIntent: Intent? = null
    var resultCode: Int = Activity.RESULT_CANCELED
}