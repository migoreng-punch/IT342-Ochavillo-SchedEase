package edu.cit.ochavillo.schedease

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.ochavillo.schedease.network.OverrideDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OverrideAdapter(
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<OverrideAdapter.ViewHolder>() {

    private var items: List<OverrideDto> = emptyList()

    fun submitList(newItems: List<OverrideDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_override, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val override = items[position]

        // 1. Format Date
        try {
            val date = LocalDate.parse(override.date)
            holder.tvDate.text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
        } catch (e: Exception) {
            holder.tvDate.text = override.date // Fallback
        }

        // 2. Format Status (Fully Unavailable vs Time Range)
        if (override.unavailable) {
            holder.tvStatus.text = "Fully Unavailable"
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444")) // Red-500
        } else {
            val start = override.startTime?.take(5) ?: "??:??"
            val end = override.endTime?.take(5) ?: "??:??"
            holder.tvStatus.text = "Available: $start - $end"
            holder.tvStatus.setTextColor(Color.parseColor("#6B7280")) // Gray-500
        }

        // 3. Delete Action
        holder.btnDelete.setOnClickListener {
            onDelete(override.id)
        }
    }

    override fun getItemCount() = items.size
}