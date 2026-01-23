package com.example.nabthespy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecordedSessionFragment : Fragment() {

    private lateinit var sessionsRecyclerView: RecyclerView
    private lateinit var emptyView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_recorded_session, container, false)

        sessionsRecyclerView = view.findViewById(R.id.sessionsRecyclerView)
        emptyView = view.findViewById(R.id.emptyView)

        sessionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onResume() {
        super.onResume()
        loadSessions()
    }

    private fun loadSessions() {
        // 🔥 GET TIMELINE SESSIONS
        val sessions = SessionManager
            .getSessions(requireContext())
            .sortedByDescending { it.timestamp } // newest first

        if (sessions.isEmpty()) {
            sessionsRecyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            sessionsRecyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE

            sessionsRecyclerView.adapter = SessionAdapter(sessions) { clickedSession ->
                val intent = Intent(requireContext(), SessionDetailActivity::class.java).apply {
                    putExtra("SNAPSHOT_PATH", clickedSession.snapshotPath)
                    putExtra("TIMESTAMP", clickedSession.timestamp)
                }
                startActivity(intent)
            }
        }
    }
}
