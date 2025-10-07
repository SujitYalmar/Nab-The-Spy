package com.example.nabthespy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class CaptureAdapter(
    private val capturePaths: List<String>,
    private val onCaptureClicked: (String) -> Unit
) : RecyclerView.Adapter<CaptureAdapter.CaptureViewHolder>() {

    class CaptureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.captureThumbnailImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaptureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.capture_item, parent, false)
        return CaptureViewHolder(view)
    }

    override fun onBindViewHolder(holder: CaptureViewHolder, position: Int) {
        val path = capturePaths[position]
        Glide.with(holder.itemView.context).load(File(path)).into(holder.imageView)
        holder.itemView.setOnClickListener {
            onCaptureClicked(path)
        }
    }

    override fun getItemCount() = capturePaths.size
}