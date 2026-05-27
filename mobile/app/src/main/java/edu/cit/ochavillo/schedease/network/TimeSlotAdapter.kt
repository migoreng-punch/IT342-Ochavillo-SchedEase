package edu.cit.ochavillo.schedease

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class TimeSlotAdapter(
    private val onTimeSelected: (String) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {

    private var slots: List<String> = emptyList()
    private var selectedTime: String? = null

    fun submitList(newSlots: List<String>) {
        slots = newSlots
        selectedTime = null // Reset selection on new dates
        notifyDataSetChanged()
    }

    fun getSelectedTime(): String? = selectedTime

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view as MaterialCardView
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val time = slots[position]
        holder.tvTime.text = formatTimeForUI(time)

        val isSelected = time == selectedTime

        if (isSelected) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.blue_600))
            holder.tvTime.setTextColor(Color.WHITE)
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.tvTime.setTextColor(Color.DKGRAY)
        }

        holder.card.setOnClickListener {
            selectedTime = time
            notifyDataSetChanged()
            onTimeSelected(time)
        }
    }

    override fun getItemCount() = slots.size

    // Quick helper to format "14:00:00" to "2:00 PM"
    private fun formatTimeForUI(time24: String): String {
        try {
            val parts = time24.split(":")
            var hour = parts[0].toInt()
            val minute = parts[1]
            val modifier = if (hour >= 12) "PM" else "AM"
            if (hour == 0) hour = 12
            if (hour > 12) hour -= 12
            return "$hour:$minute $modifier"
        } catch (e: Exception) {
            return time24
        }
    }
}