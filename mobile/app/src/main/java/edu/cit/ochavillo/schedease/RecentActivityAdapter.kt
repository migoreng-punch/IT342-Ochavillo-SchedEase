package edu.cit.ochavillo.schedease

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import edu.cit.ochavillo.schedease.network.ProviderAppointmentResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class RecentActivityAdapter : RecyclerView.Adapter<RecentActivityAdapter.ViewHolder>() {

    private var items: List<ProviderAppointmentResponse> = emptyList()

    fun submitList(newItems: List<ProviderAppointmentResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvClientName)
        val tvDate: TextView = view.findViewById(R.id.tvDateTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val cvStatusBadge: MaterialCardView = view.findViewById(R.id.cvStatusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apt = items[position]

        holder.tvName.text = apt.clientName ?: "Unknown Client"

        // Format Date and Time
        try {
            val date = LocalDate.parse(apt.appointmentDate).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            val time = LocalTime.parse(apt.startTime).format(DateTimeFormatter.ofPattern("h:mm a"))
            holder.tvDate.text = "$date at $time"
        } catch (e: Exception) {
            holder.tvDate.text = "${apt.appointmentDate} at ${apt.startTime}"
        }

        // Status Badge Logic
        holder.tvStatus.text = apt.status.uppercase()
        when (apt.status.uppercase()) {
            "CONFIRMED" -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#D1FAE5"))
                holder.tvStatus.setTextColor(Color.parseColor("#065F46"))
            }
            "PENDING" -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#FEF3C7"))
                holder.tvStatus.setTextColor(Color.parseColor("#92400E"))
            }
            "CANCELLED" -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#FEE2E2"))
                holder.tvStatus.setTextColor(Color.parseColor("#991B1B"))
            }
            else -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#F3F4F6"))
                holder.tvStatus.setTextColor(Color.parseColor("#374151"))
            }
        }
    }

    override fun getItemCount() = items.size
}