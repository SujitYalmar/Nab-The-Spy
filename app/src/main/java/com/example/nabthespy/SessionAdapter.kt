package com.example.nabthespy

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

data class RecordedSession(val snapshotPath: String, val timestamp: String)

class SessionAdapter(
    private val sessions: List<RecordedSession>,
    private val onSessionClicked: (RecordedSession) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val snapshotImageView: ImageView = itemView.findViewById(R.id.snapshotImageView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.session_item, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.timestampTextView.text = session.timestamp

        Glide.with(holder.itemView.context)
            .load(File(session.snapshotPath))
            .into(holder.snapshotImageView)

        // Pass the click event back to the fragment
        holder.itemView.setOnClickListener {
            onSessionClicked(session)
        }
    }

    override fun getItemCount() = sessions.size
}