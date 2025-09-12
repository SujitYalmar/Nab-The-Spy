package com.example.nabthespy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class FaqFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // This connects the Kotlin file to its XML layout.
        return inflater.inflate(R.layout.fragment_faq, container, false)
    }
}