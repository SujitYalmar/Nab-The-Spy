package com.example.nabthespy

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- 1. SET UP THE TOP APP BAR (TOOLBAR) ---
        val topAppBar: Toolbar = findViewById(R.id.top_app_bar)
        setSupportActionBar(topAppBar)
        // -----------------------------------------

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set the default fragment and title
        if (savedInstanceState == null) {
            supportActionBar?.title = "NabTheSpy" // Set initial title
            loadFragment(HomeFragment())
        }

        // Set the listener for navigation item selections
        bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment
            // --- CHANGE IS HERE ---
            // Update the title based on the selected item
            when (item.itemId) {
                R.id.nav_about -> {
                    supportActionBar?.title = "About Us"
                    selectedFragment = AboutFragment()
                }
                R.id.nav_faq -> {
                    supportActionBar?.title = "FAQ"
                    selectedFragment = FaqFragment()
                }
                else -> { // R.id.nav_home
                    supportActionBar?.title = "Control Panel"
                    selectedFragment = HomeFragment()
                }
            }
            loadFragment(selectedFragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}