package com.example.nabthespy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.nabthespy.util.MediaProjectionManager as AppMediaProjectionManager
import com.google.android.material.appbar.MaterialToolbar // CHANGED THIS import
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private val mediaProjectionPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            AppMediaProjectionManager.resultCode = result.resultCode
            AppMediaProjectionManager.projectionIntent = result.data
            Toast.makeText(this, "Permission granted. NabTheSpy is active.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied. Intruder capturing will not work.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // --- UPDATED to match your XML layout ---
        val toolbar: MaterialToolbar = findViewById(R.id.top_app_bar) // CHANGED THIS line
        setSupportActionBar(toolbar)

        sessionManager = SessionManager(this)
        // ------------------------------------------

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null

            when (item.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_sessions -> selectedFragment = RecordedSessionFragment()
                R.id.nav_about -> selectedFragment = AboutFragment()
                R.id.nav_faq -> selectedFragment = FaqFragment()
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment)
            }
            true
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_home
        }

        if (AppMediaProjectionManager.projectionIntent == null) {
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjectionPermissionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val userDetails = sessionManager.getUserDetails()
                val pin = userDetails[SessionManager.KEY_USER_PIN]
                Toast.makeText(this, "Profile: Logged in with PIN: $pin", Toast.LENGTH_LONG).show()
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