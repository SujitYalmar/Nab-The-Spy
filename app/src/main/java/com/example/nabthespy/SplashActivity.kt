package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    // ADD THIS: Declare your SessionManager
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You can optionally set a layout for your splash screen
        // setContentView(R.layout.activity_splash)

        // ADD THIS: Initialize the SessionManager
        sessionManager = SessionManager(this)

        // Using a Handler to show the splash screen for a moment
        Handler(Looper.getMainLooper()).postDelayed({
            // Decide which activity to launch using the SessionManager
            val intent = if (sessionManager.isLoggedIn()) {
                // If logged in, go to the HomeActivity
                Intent(this, HomeActivity::class.java)
            } else {
                // If not logged in, go to the KeyLoginActivity
                Intent(this, KeyLoginActivity::class.java)
            }

            // Start the determined activity
            startActivity(intent)

            // Finish SplashActivity so the user can't navigate back to it
            finish()
        }, 1500) // 1.5 second delay
    }
}