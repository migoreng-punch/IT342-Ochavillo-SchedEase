package edu.cit.ochavillo.schedease

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import edu.cit.ochavillo.schedease.network.AppointmentResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AppointmentAdapter(
    private val onCancel: (String) -> Unit,
    private val onReschedule: (String) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    private var items: List<AppointmentResponse> = emptyList()

    fun submitList(newItems: List<AppointmentResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvEstName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val cvStatusBadge: MaterialCardView = view.findViewById(R.id.cvStatusBadge)
        val layoutActions: LinearLayout = view.findViewById(R.id.layoutActions)
        val btnReschedule: ImageButton = view.findViewById(R.id.btnReschedule)
        val btnCancel: ImageButton = view.findViewById(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apt = items[position]

        // 1. Basic Info
        holder.tvName.text = apt.establishmentName ?: "Unknown Establishment"

        // 2. Formatters (Replicating formatters.js using java.time)
        try {
            val date = LocalDate.parse(apt.appointmentDate)
            holder.tvDate.text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))

            val time = LocalTime.parse(apt.startTime)
            holder.tvTime.text = time.format(DateTimeFormatter.ofPattern("h:mm a"))
        } catch (e: Exception) {
            holder.tvDate.text = apt.appointmentDate
            holder.tvTime.text = apt.startTime
        }

        // 3. Status Badge Styling
        holder.tvStatus.text = apt.status.uppercase()
        when (apt.status.uppercase()) {
            "CONFIRMED" -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#D1FAE5")) // Green-100
                holder.tvStatus.setTextColor(Color.parseColor("#065F46")) // Green-800
            }
            "PENDING" -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#FEF3C7")) // Yellow-100
                holder.tvStatus.setTextColor(Color.parseColor("#92400E")) // Yellow-800
            }
            "CANCELLED" -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#FEE2E2")) // Red-100
                holder.tvStatus.setTextColor(Color.parseColor("#991B1B")) // Red-800
            }
            else -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#F3F4F6")) // Gray-100
                holder.tvStatus.setTextColor(Color.parseColor("#374151")) // Gray-700
            }
        }

        // 4. Action Buttons Visibility
        val isActive = apt.status.equals("CONFIRMED", ignoreCase = true) ||
                apt.status.equals("PENDING", ignoreCase = true)

        holder.layoutActions.visibility = if (isActive) View.VISIBLE else View.GONE

        // 5. Click Listeners
        holder.btnCancel.setOnClickListener { onCancel(apt.id) }
        holder.btnReschedule.setOnClickListener { onReschedule(apt.id) }
    }

    override fun getItemCount() = items.size
}