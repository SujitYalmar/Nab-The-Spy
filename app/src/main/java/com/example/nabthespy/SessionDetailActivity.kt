package com.example.nabthespy

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_detail)

        val imageView: ImageView = findViewById(R.id.detailSnapshotImageView)
        val timeText: TextView = findViewById(R.id.detailTimestampTextView)
        val deleteButton: Button = findViewById(R.id.deleteButton)

        val imagePath = intent.getStringExtra("SNAPSHOT_PATH")
        val timestamp = intent.getLongExtra("TIMESTAMP", 0L)

        if (imagePath == null || !File(imagePath).exists()) {
            Toast.makeText(this, "Session image not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 🖼 Show image
        Glide.with(this)
            .load(File(imagePath))
            .into(imageView)

        // 🕒 Show time
        val formattedTime = SimpleDateFormat(
            "dd MMM yyyy • hh:mm a",
            Locale.getDefault()
        ).format(Date(timestamp))

        timeText.text = formattedTime

        // 🗑 Delete session
        deleteButton.setOnClickListener {
            deleteSession(imagePath)
            Toast.makeText(this, "Session deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun deleteSession(imagePath: String) {
        // Remove image file
        File(imagePath).delete()

        // Remove session from SessionManager
        val sessions = SessionManager
            .getSessions(this)
            .filterNot { it.snapshotPath == imagePath }

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit()
            .putString(
                "sessions",
                com.google.gson.Gson().toJson(sessions)
            )
            .apply()
    }
}
