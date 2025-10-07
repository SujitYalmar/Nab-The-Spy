package com.example.nabthespy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class RecordedSessionFragment : Fragment() {

    private lateinit var sessionsRecyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var sessionManager: SessionManager

    // This launcher starts the detail activity and waits for a result.
    private val sessionDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // This code runs when the detail activity closes.
        if (result.resultCode == Activity.RESULT_OK) {
            // If a session was deleted, we refresh the list.
            setupRecyclerView()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recorded_session, container, false)

        sessionsRecyclerView = view.findViewById(R.id.sessionsRecyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        sessionManager = SessionManager(requireContext())

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list every time the fragment is shown
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val sessions = sessionManager.getAllSessions()

        if (sessions.isEmpty()) {
            sessionsRecyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            sessionsRecyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE

            // ## THIS BLOCK IS NOW CORRECT ##
            // We pass a function to the adapter for it to call when an item is clicked.
            sessionsRecyclerView.layoutManager = LinearLayoutManager(context)
            sessionsRecyclerView.adapter = SessionAdapter(sessions) { clickedSession ->
                // This is the code that runs when a session item is clicked.
                val intent = Intent(requireContext(), SessionDetailActivity::class.java).apply {
                    putExtra("SNAPSHOT_PATH", clickedSession.snapshotPath)
                }
                // We use our new launcher to start the activity.
                sessionDetailLauncher.launch(intent)
            }
        }
    }
}