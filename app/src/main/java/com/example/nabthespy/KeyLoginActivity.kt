package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nabthespy.util.SecureStorageHelper

class KeyLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_login)

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

                // Save login state
                val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putBoolean("is_logged_in", true)
                    apply()
                }

                // --- CHANGE IS HERE ---
                // Start MainActivity instead of HomeActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Incorrect PIN. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}