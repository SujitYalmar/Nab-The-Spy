package com.example.nabthespy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var sessionManager: SessionManager
    private lateinit var switchWatchMode: SwitchMaterial

    // This launcher handles the results of asking for permissions.
    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // This code runs after the user responds to the permission dialog
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                // If they granted everything, we can now enable the feature
                enableWatchMode(true)
                switchWatchMode.isChecked = true
            } else {
                Toast.makeText(requireContext(), "All permissions are required for Watch Mode.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        switchWatchMode = view.findViewById(R.id.monitoring_switch)

        // Set the switch's initial state
        switchWatchMode.isChecked = sessionManager.isWatchModeEnabled()

        // When the user taps the switch...
        switchWatchMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // ...if they are turning it ON, check for all permissions first.
                checkAndRequestPermissions()
            } else {
                // ...if they are turning it OFF, just disable it.
                enableWatchMode(false)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        // --- Step 1: Check for "Display over other apps" ---
        if (!Settings.canDrawOverlays(requireContext())) {
            Toast.makeText(requireContext(), "Please grant 'Display over other apps' permission", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireActivity().packageName}")
            )
            startActivity(intent)
            switchWatchMode.isChecked = false
            return
        }

        // --- Step 2: Check for standard runtime permissions ---
        val permissionsToRequest = mutableListOf<String>()

        // Notification permission is REQUIRED for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        // --- Step 3: Act on the results ---
        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
            switchWatchMode.isChecked = false
        } else {
            enableWatchMode(true)
        }
    }

    private fun enableWatchMode(isEnabled: Boolean) {
        sessionManager.setWatchModeEnabled(isEnabled)
        val status = if (isEnabled) "Enabled" else "Disabled"
        Toast.makeText(requireContext(), "Monitoring $status", Toast.LENGTH_SHORT).show()
    }
}

