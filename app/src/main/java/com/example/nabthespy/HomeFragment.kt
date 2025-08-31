package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the buttons using the fragment's 'view'
        val btnSetupPin = view.findViewById<Button>(R.id.btnSetupPin)
        val btnLoginPin = view.findViewById<Button>(R.id.btnLoginPin)

        // Set the click listeners
        btnSetupPin.setOnClickListener {
            val intent = Intent(requireContext(), KeySetupActivity::class.java)
            startActivity(intent)
        }

        btnLoginPin.setOnClickListener {
            val intent = Intent(requireContext(), KeyLoginActivity::class.java)
            startActivity(intent)
        }
    }
}