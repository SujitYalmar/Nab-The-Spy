package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Access the shared preferences to check the login state
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)

        // Decide which activity to launch
        val intent = if (isLoggedIn) {
            // If logged in, go to the MainActivity
            Intent(this, MainActivity::class.java)
        } else {
            // If not logged in, go to the KeyLoginActivity
            Intent(this, KeyLoginActivity::class.java)
        }

        // Start the determined activity
        startActivity(intent)

        // Finish SplashActivity so the user can't navigate back to it
        finish()
    }
}