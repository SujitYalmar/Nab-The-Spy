package com.example.nabthespy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionAdapter(
    private val sessions: List<Session>,
    private val onSessionClicked: (Session) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val snapshotImageView: ImageView =
            itemView.findViewById(R.id.snapshotImageView)
        val timestampTextView: TextView =
            itemView.findViewById(R.id.timestampTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.session_item, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]

        // 🕒 Format timestamp for UI
        val formattedTime = SimpleDateFormat(
            "dd MMM yyyy • hh:mm a",
            Locale.getDefault()
        ).format(Date(session.timestamp))

        holder.timestampTextView.text = formattedTime

        // 🖼 Load captured image
        Glide.with(holder.itemView.context)
            .load(File(session.snapshotPath))
            .centerCrop()
            .into(holder.snapshotImageView)

        holder.itemView.setOnClickListener {
            onSessionClicked(session)
        }
    }

    override fun getItemCount(): Int = sessions.size
}
