// EstablishmentAdapter.kt
package edu.cit.ochavillo.schedease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import edu.cit.ochavillo.schedease.network.Establishment

class EstablishmentAdapter(
    private val onViewAvailabilityClick: (Establishment) -> Unit
) : RecyclerView.Adapter<EstablishmentAdapter.ViewHolder>() {

    private var items: List<Establishment> = emptyList()

    fun submitList(newItems: List<Establishment>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvEstName)
        val tvDesc: TextView = view.findViewById(R.id.tvEstDesc)
        val btnView: MaterialButton = view.findViewById(R.id.btnViewAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_establishment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val establishment = items[position]
        holder.tvName.text = establishment.name
        holder.tvDesc.text = establishment.description

        holder.btnView.setOnClickListener {
            onViewAvailabilityClick(establishment)
        }
    }

    override fun getItemCount() = items.size
}