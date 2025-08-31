package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nabthespy.SessionManager
import com.example.nabthespy.util.SecureStorageHelper

class KeyLoginActivity : AppCompatActivity() {

    // 1. Add a variable for our SessionManager
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_login)

        // 2. Initialize the SessionManager
        sessionManager = SessionManager(this)

        val etPin = findViewById<EditText>(R.id.etLoginPin)
        val btnLogin = findViewById<Button>(R.id.btnLoginPin)

        btnLogin.setOnClickListener {
            val enteredPin = etPin.text.toString()
            val storedPin = SecureStorageHelper.getPin(this)

            if (storedPin == null) {
                Toast.makeText(this, "No PIN found. Please set up a PIN.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, KeySetupActivity::class.java))
                finish()
            } else if (enteredPin == storedPin) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                // --- FIX #1: Use SessionManager ---
                // This ensures SplashActivity knows the user is logged in.
                sessionManager.setLogin(true)

                // --- FIX #2: Navigate to HomeActivity ---
                // This sends the user to the correct home page.
                val intent = Intent(this, HomeActivity::class.java)

                // Add flags to clear the navigation history so the user can't go back to the login screen.
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Incorrect PIN. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

