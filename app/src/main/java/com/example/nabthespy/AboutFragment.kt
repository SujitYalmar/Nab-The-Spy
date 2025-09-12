package com.example.nabthespy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // This line connects the Kotlin file to its XML layout file.
        return inflater.inflate(R.layout.fragment_about, container, false)
    }
}