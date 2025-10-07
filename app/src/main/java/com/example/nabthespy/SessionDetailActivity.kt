package com.example.nabthespy

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class SessionDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_detail)

        val mainSnapshotImageView: ImageView = findViewById(R.id.detailSnapshotImageView)
        val capturesRecyclerView: RecyclerView = findViewById(R.id.capturesRecyclerView)
        val deleteButton: Button = findViewById(R.id.deleteButton)

        val snapshotPath = intent.getStringExtra("SNAPSHOT_PATH")
        val sessionDir = snapshotPath?.let { File(it).parentFile }

        if (sessionDir != null && sessionDir.exists()) {
            // Load the main intruder snapshot
            Glide.with(this).load(File(snapshotPath)).into(mainSnapshotImageView)

            // Find all screen captures, sort them by name
            val captureFiles = sessionDir.listFiles { _, name -> name.startsWith("capture_") }
                ?.map { it.absolutePath }
                ?.sorted()
                ?: emptyList()

            // Set up the horizontal RecyclerView for thumbnails
            capturesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            capturesRecyclerView.adapter = CaptureAdapter(captureFiles) { clickedCapturePath ->
                // When a thumbnail is clicked, update the main image view
                Glide.with(this).load(File(clickedCapturePath)).into(mainSnapshotImageView)
            }

            // Set up the delete button
            deleteButton.setOnClickListener {
                sessionDir.deleteRecursively()
                Toast.makeText(this, "Session deleted", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}