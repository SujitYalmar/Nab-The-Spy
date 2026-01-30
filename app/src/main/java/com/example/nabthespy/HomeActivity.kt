package com.example.nabthespy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.nabthespy.util.MediaProjectionManager as AppMediaProjectionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    /* ---------------------------------------------------------
     * 🎥 MediaProjection Permission Launcher
     * --------------------------------------------------------- */
    private val mediaProjectionPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK && result.data != null) {

                AppMediaProjectionManager.resultCode = result.resultCode
                AppMediaProjectionManager.projectionIntent = result.data

                Toast.makeText(
                    this,
                    "Screen capture permission granted",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                AppMediaProjectionManager.resultCode = 0
                AppMediaProjectionManager.projectionIntent = null

                Toast.makeText(
                    this,
                    "Screen capture denied. Intruder screenshots will not work.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        /* ---------------------------------------------------------
         * 🔝 Toolbar
         * --------------------------------------------------------- */
        val toolbar: MaterialToolbar = findViewById(R.id.top_app_bar)
        setSupportActionBar(toolbar)

        sessionManager = SessionManager(this)

        /* ---------------------------------------------------------
         * 🔽 Bottom Navigation
         * --------------------------------------------------------- */
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_sessions -> RecordedSessionFragment()
                R.id.nav_about -> AboutFragment()
                R.id.nav_faq -> FaqFragment()
                else -> null
            }

            fragment?.let { loadFragment(it) }
            true
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_home
        }

        /* ---------------------------------------------------------
         * 🛂 Request Screen Capture Permission (ONCE)
         * --------------------------------------------------------- */
        requestMediaProjectionPermissionIfNeeded()
    }

    private fun requestMediaProjectionPermissionIfNeeded() {
        if (AppMediaProjectionManager.projectionIntent != null) {
            Log.d("HomeActivity", "MediaProjection already granted")
            return
        }

        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        mediaProjectionPermissionLauncher.launch(
            mediaProjectionManager.createScreenCaptureIntent()
        )
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /* ---------------------------------------------------------
     * ☰ Menu
     * --------------------------------------------------------- */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_profile -> {
                val userDetails = sessionManager.getUserDetails()
                val pin = userDetails[SessionManager.KEY_USER_PIN]
                Toast.makeText(
                    this,
                    "Profile: Logged in with PIN: $pin",
                    Toast.LENGTH_LONG
                ).show()
                true
            }

            R.id.action_logout -> {
                sessionManager.logoutUser()
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
