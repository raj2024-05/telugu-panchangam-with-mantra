package com.example.panchang

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeatureAdapter(
    private val items: List<FeatureItem>
) : RecyclerView.Adapter<FeatureAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.tvEmoji)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val desc: TextView = view.findViewById(R.id.tvDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feature_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.emoji.text = item.emoji
        holder.title.text = item.title
        holder.desc.text = item.description
        holder.itemView.setOnClickListener {
            item.target?.let { cls ->
                val ctx = holder.itemView.context
                ctx.startActivity(Intent(ctx, cls))
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
