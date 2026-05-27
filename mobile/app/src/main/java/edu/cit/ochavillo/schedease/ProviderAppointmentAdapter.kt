package edu.cit.ochavillo.schedease

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import edu.cit.ochavillo.schedease.network.ProviderAppointmentResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ProviderAppointmentAdapter(
    private val onUpdateStatus: (String, String) -> Unit
) : RecyclerView.Adapter<ProviderAppointmentAdapter.ViewHolder>() {

    private var items: List<ProviderAppointmentResponse> = emptyList()

    fun submitList(newItems: List<ProviderAppointmentResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClientName: TextView = view.findViewById(R.id.tvClientName)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val cvStatusBadge: MaterialCardView = view.findViewById(R.id.cvStatusBadge)

        val layoutActionsPending: LinearLayout = view.findViewById(R.id.layoutActionsPending)
        val layoutActionsConfirmed: LinearLayout = view.findViewById(R.id.layoutActionsConfirmed)
        val tvNoActions: TextView = view.findViewById(R.id.tvNoActions)

        val btnConfirm: MaterialButton = view.findViewById(R.id.btnConfirm)
        val btnCancelPending: MaterialButton = view.findViewById(R.id.btnCancelPending)
        val btnComplete: MaterialButton = view.findViewById(R.id.btnComplete)
        val btnCancelConfirmed: MaterialButton = view.findViewById(R.id.btnCancelConfirmed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_provider_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apt = items[position]

        holder.tvClientName.text = apt.clientName ?: "Unknown Client"

        try {
            val date = LocalDate.parse(apt.appointmentDate).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            val time = LocalTime.parse(apt.startTime).format(DateTimeFormatter.ofPattern("h:mm a"))
            holder.tvDateTime.text = "$date at $time"
        } catch (e: Exception) {
            holder.tvDateTime.text = "${apt.appointmentDate} at ${apt.startTime}"
        }

        // Status Badge Logic
        val statusUpper = apt.status.uppercase()
        holder.tvStatus.text = statusUpper
        when (statusUpper) {
            "CONFIRMED" -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#D1FAE5"))
                holder.tvStatus.setTextColor(Color.parseColor("#065F46"))
            }
            "PENDING" -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#FEF3C7"))
                holder.tvStatus.setTextColor(Color.parseColor("#92400E"))
            }
            "COMPLETED" -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#F3F4F6"))
                holder.tvStatus.setTextColor(Color.parseColor("#374151"))
            }
            "CANCELLED" -> {
                holder.cvStatusBadge.setCardBackgroundColor(Color.parseColor("#FEE2E2"))
                holder.tvStatus.setTextColor(Color.parseColor("#991B1B"))
            }
        }

        // Action Buttons Visibility Logic
        holder.layoutActionsPending.visibility = if (statusUpper == "PENDING") View.VISIBLE else View.GONE
        holder.layoutActionsConfirmed.visibility = if (statusUpper == "CONFIRMED") View.VISIBLE else View.GONE
        holder.tvNoActions.visibility = if (statusUpper == "COMPLETED" || statusUpper == "CANCELLED") View.VISIBLE else View.GONE

        // Click Listeners
        holder.btnConfirm.setOnClickListener { onUpdateStatus(apt.id, "CONFIRMED") }
        holder.btnCancelPending.setOnClickListener { onUpdateStatus(apt.id, "CANCELLED") }
        holder.btnComplete.setOnClickListener { onUpdateStatus(apt.id, "COMPLETED") }
        holder.btnCancelConfirmed.setOnClickListener { onUpdateStatus(apt.id, "CANCELLED") }
    }

    override fun getItemCount() = items.size
}