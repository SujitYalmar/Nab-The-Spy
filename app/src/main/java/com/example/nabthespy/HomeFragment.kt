package com.example.nabthespy

import android.content.ComponentName
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial


class HomeFragment : Fragment() {

    private lateinit var switchMonitoring: SwitchMaterial
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireActivity().getSharedPreferences("AppPrefs", AppCompatActivity.MODE_PRIVATE)

        // ## THIS IS THE FIX ##
        // Find the switch in the layout BEFORE using it.
        switchMonitoring = view.findViewById(R.id.monitoring_switch)

        // Now it's safe to use the switch
        val isMonitoringEnabled = prefs.getBoolean("isMonitoringEnabled", true)
        switchMonitoring.isChecked = isMonitoringEnabled
        setMonitoringService(isMonitoringEnabled)

        switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("isMonitoringEnabled", isChecked).apply()
            setMonitoringService(isChecked)

            if (isChecked) {
                Toast.makeText(context, "Monitoring Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Monitoring Disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setMonitoringService(enabled: Boolean) {
        val receiver = ComponentName(requireContext(), UnlockReceiver::class.java)
        val packageManager = requireActivity().packageManager

        val newState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        packageManager.setComponentEnabledSetting(
            receiver,
            newState,
            PackageManager.DONT_KILL_APP
        )
    }
}