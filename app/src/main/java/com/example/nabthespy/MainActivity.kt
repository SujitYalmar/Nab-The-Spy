package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nabthespy.util.SecureStorageHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user has already set up a PIN
        if (SecureStorageHelper.isPinSet(this)) {
            // If PIN is set, go directly to the Login screen
            val intent = Intent(this, KeyLoginActivity::class.java)
            startActivity(intent)
        } else {
            // If no PIN is set, go to the setup screen
            val intent = Intent(this, KeySetupActivity::class.java)
            startActivity(intent)
        }

        // Finish MainActivity so the user can't go back to it
        finish()
    }
}