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

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_login)

        sessionManager = SessionManager(this)

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

                // CHANGE THIS LINE:
                // Instead of just setting login to true, we now also save the user's PIN.
                sessionManager.createLoginSession(enteredPin)

                // Now, navigate to the HomeActivity.
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