// File: com/example/nabthespy/KeyLoginActivity.kt

package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nabthespy.util.SecureStorageHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class KeyLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_login)

        // SessionManager has been removed as it's not needed.

        val etPin = findViewById<TextInputEditText>(R.id.etLoginPin)
        val btnLogin = findViewById<Button>(R.id.btnLoginPin)
        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)

        btnLogin.setOnClickListener {
            val enteredPin = etPin.text.toString()
            val storedPin = SecureStorageHelper.getPin(this)

            if (storedPin == null) {
                Toast.makeText(this, "No account found. Please create one.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, KeySetupActivity::class.java))
            } else if (enteredPin == storedPin) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                // Simply navigate to the HomeActivity.
                // The app's persistent "logged in" state is already handled by
                // the 'is_setup_complete' flag you set during face registration.
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Incorrect PIN. Try again.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, KeySetupActivity::class.java))
        }
    }
}