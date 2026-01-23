package com.example.nabthespy

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import com.example.nabthespy.util.SecureStorageHelper

class KeySetupActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_setup)

        // Device Admin setup
        devicePolicyManager =
            getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, SecurityAdminReceiver::class.java)

        // UI elements
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

                    // Now ensure Device Admin is enabled
                    if (!devicePolicyManager.isAdminActive(adminComponent)) {
                        requestDeviceAdmin()
                    } else {
                        goToFaceRegistration()
                    }
                }
            }
        }
    }

    private fun requestDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "This permission allows NabdaSpy to detect unauthorized unlock attempts and protect your device."
            )
        }
        startActivityForResult(intent, 201)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 201) {
            if (devicePolicyManager.isAdminActive(adminComponent)) {
                Toast.makeText(
                    this,
                    "Security protection enabled successfully",
                    Toast.LENGTH_SHORT
                ).show()
                goToFaceRegistration()
            } else {
                Toast.makeText(
                    this,
                    "Device security permission is required to continue",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @ExperimentalGetImage
    private fun goToFaceRegistration() {
        Toast.makeText(
            this,
            "PIN setup complete. Please register your face.",
            Toast.LENGTH_LONG
        ).show()

        startActivity(Intent(this, FaceRegistrationActivity::class.java))
        finish()
    }
}
