package com.example.nabthespy

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var btnToggleWatchMode: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnToggleWatchMode = view.findViewById(R.id.btnToggleWatchMode)

        // Set the initial button text when the screen loads
        updateButtonState()

        btnToggleWatchMode.setOnClickListener {
            if (isServiceRunning(WatchModeService::class.java)) {
                // If the service is currently running, stop it
                stopWatchModeService()
            } else {
                // If the service is not running, start it
                startWatchModeService()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update the button state every time the user returns to this screen
        updateButtonState()
    }

    private fun startWatchModeService() {
        Toast.makeText(requireContext(), "Watch Mode Started", Toast.LENGTH_SHORT).show()
        val serviceIntent = Intent(requireContext(), WatchModeService::class.java)
        // Use startForegroundService for modern Android versions
        ContextCompat.startForegroundService(requireContext(), serviceIntent)
        updateButtonState()
    }

    private fun stopWatchModeService() {
        Toast.makeText(requireContext(), "Watch Mode Stopped", Toast.LENGTH_SHORT).show()
        val serviceIntent = Intent(requireContext(), WatchModeService::class.java)
        requireContext().stopService(serviceIntent)
        updateButtonState()
    }

    private fun updateButtonState() {
        if (isServiceRunning(WatchModeService::class.java)) {
            btnToggleWatchMode.text = "Stop Watch Mode"
        } else {
            btnToggleWatchMode.text = "Start Watch Mode"
        }
    }

    // A helper function to check if our service is currently running
    @Suppress("DEPRECATION")


    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}

