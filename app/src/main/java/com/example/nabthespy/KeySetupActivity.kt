package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nabthespy.util.SecureStorageHelper

class KeySetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_setup)

        // Initialize UI elements
        val etPin = findViewById<EditText>(R.id.etPin)
        val etConfirmPin = findViewById<EditText>(R.id.etConfirmPin)
        val btnSave = findViewById<Button>(R.id.btnSavePin)

        btnSave.setOnClickListener {
            val pin = etPin.text.toString()
            val confirmPin = etConfirmPin.text.toString()

            when {
                pin.length != 8 -> {
                    Toast.makeText(this, "PIN must be 8 digits", Toast.LENGTH_SHORT).show()
                }
                pin != confirmPin -> {
                    Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Save PIN securely
                    SecureStorageHelper.savePin(this, pin)
                    Toast.makeText(this, "PIN setup successful", Toast.LENGTH_SHORT).show()

                    // Save login state
                    val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putBoolean("is_logged_in", true)
                        apply()
                    }

                    // --- CHANGE IS HERE ---
                    // Redirect to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}