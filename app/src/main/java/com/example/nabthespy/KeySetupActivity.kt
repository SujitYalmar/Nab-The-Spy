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

        // Initialize UI elements from your activity_key_setup.xml
        val etPin = findViewById<EditText>(R.id.etPin)
        val etConfirmPin = findViewById<EditText>(R.id.etConfirmPin)
        val btnSave = findViewById<Button>(R.id.btnSavePin)

        btnSave.setOnClickListener {
            val pin = etPin.text.toString()
            val confirmPin = etConfirmPin.text.toString()

            when {
                // 1. Validate PIN length
                pin.length != 8 -> {
                    Toast.makeText(this, "PIN must be 8 digits", Toast.LENGTH_SHORT).show()
                }
                // 2. Validate that PINs match
                pin != confirmPin -> {
                    Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show()
                }
                // 3. If all checks pass, proceed
                else -> {
                    // Save the PIN securely
                    SecureStorageHelper.savePin(this, pin)

                    // Inform the user about the next step
                    Toast.makeText(
                        this,
                        "PIN setup successful. Next, register your face.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Redirect to FaceRegistrationActivity to continue the setup process
                    val intent = Intent(this, FaceRegistrationActivity::class.java)
                    startActivity(intent)

                    // Finish this activity so the user cannot go back to it
                    finish()
                }
            }
        }
    }
}