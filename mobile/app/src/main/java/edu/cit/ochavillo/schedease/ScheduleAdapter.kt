package edu.cit.ochavillo.schedease

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import java.util.Locale

class ScheduleAdapter(
    private val onToggleChanged: (String, Boolean) -> Unit,
    private val onTimeClicked: (String, Boolean, String) -> Unit // dayOfWeek, isStartTime, currentTime
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    private var items: List<ScheduleDay> = emptyList()

    fun submitList(newItems: List<ScheduleDay>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val switchDay: MaterialSwitch = view.findViewById(R.id.switchDay)
        val tvDayName: TextView = view.findViewById(R.id.tvDayName)
        val layoutTimes: LinearLayout = view.findViewById(R.id.layoutTimes)
        val tvStartTime: TextView = view.findViewById(R.id.tvStartTime)
        val tvEndTime: TextView = view.findViewById(R.id.tvEndTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = items[position]

        // Format "MONDAY" to "Monday"
        holder.tvDayName.text = day.dayOfWeek.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }

        // Temporarily remove listener to prevent infinite loops during recycling
        holder.switchDay.setOnCheckedChangeListener(null)
        holder.switchDay.isChecked = day.isWorkingDay
        holder.switchDay.setOnCheckedChangeListener { _, isChecked ->
            onToggleChanged(day.dayOfWeek, isChecked)
        }

        // Apply disabled styling
        holder.tvDayName.setTextColor(if (day.isWorkingDay) Color.parseColor("#111827") else Color.parseColor("#9CA3AF"))
        holder.layoutTimes.alpha = if (day.isWorkingDay) 1.0f else 0.4f
        holder.tvStartTime.isEnabled = day.isWorkingDay
        holder.tvEndTime.isEnabled = day.isWorkingDay

        holder.tvStartTime.text = day.startTime.ifBlank { "--:--" }
        holder.tvEndTime.text = day.endTime.ifBlank { "--:--" }

        holder.tvStartTime.setOnClickListener {
            if (day.isWorkingDay) onTimeClicked(day.dayOfWeek, true, day.startTime)
        }

        holder.tvEndTime.setOnClickListener {
            if (day.isWorkingDay) onTimeClicked(day.dayOfWeek, false, day.endTime)
        }
    }

    override fun getItemCount() = items.size
}