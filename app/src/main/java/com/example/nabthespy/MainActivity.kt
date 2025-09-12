package com.example.nabthespy

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.nabthespy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the top app bar
        setSupportActionBar(binding.topAppBar)

        // Load the HomeFragment by default when the app starts
        if (savedInstanceState == null) {
            supportActionBar?.title = "Control Panel"
            loadFragment(HomeFragment())
        }

        // --- THIS IS THE CRITICAL CODE THAT MAKES NAVIGATION WORK ---
        // It listens for which item is clicked in the bottom navigation bar
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // When the Home item is clicked
                R.id.nav_home -> {
                    supportActionBar?.title = "Control Panel"
                    loadFragment(HomeFragment())
                    true
                }
                // When the About item is clicked
                R.id.nav_about -> {
                    supportActionBar?.title = "About Us"
                    loadFragment(AboutFragment())
                    true
                }
                // When the FAQ item is clicked
                R.id.nav_faq -> {
                    supportActionBar?.title = "FAQ"
                    loadFragment(FaqFragment())
                    true
                }
                else -> false
            }
        }
    }

    // A helper function to replace the current fragment with a new one
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    // --- Optional: Code for handling clicks on the top app bar menu (e.g., notifications) ---
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