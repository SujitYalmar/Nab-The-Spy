// File: com/example/nabthespy/HomeFragment.kt

package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial

class HomeFragment : Fragment() {

    private lateinit var switchMonitoring: SwitchMaterial
    private lateinit var tvMonitoringStatus: TextView
    private lateinit var btnViewLogs: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
//        switchMonitoring = view.findViewById(R.id.switchMonitoring)
//        tvMonitoringStatus = view.findViewById(R.id.tvMonitoringStatus)
//        btnViewLogs = view.findViewById(R.id.btnViewLogs)

        // Set listener for the monitoring switch
        switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startMonitoringService()
            } else {
                stopMonitoringService()
            }
        }

        // Set listener for the view logs button
        btnViewLogs.setOnClickListener {
            // We will create the IntrusionLogsActivity in a later step
            // val intent = Intent(requireActivity(), IntrusionLogsActivity::class.java)
            // startActivity(intent)
        }
    }

    private fun startMonitoringService() {
        tvMonitoringStatus.text = "Service is active"
        // TODO: In the next step, we will create and start the SnooperDetectionService
        // val serviceIntent = Intent(requireActivity(), SnooperDetectionService::class.java)
        // requireActivity().startService(serviceIntent)
    }

    private fun stopMonitoringService() {
        tvMonitoringStatus.text = "Service is inactive"
        // TODO: In the next step, we will stop the SnooperDetectionService
        // val serviceIntent = Intent(requireActivity(), SnooperDetectionService::class.java)
        // requireActivity().stopService(serviceIntent)
    }
}