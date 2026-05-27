package edu.cit.ochavillo.schedease

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MyAppointmentsActivity : AppCompatActivity() {

    private val viewModel: MyAppointmentsViewModel by viewModels()
    private lateinit var adapter: AppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_appointments)

        HeaderManager(this).setupHeader(R.id.mainToolbar)

        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)
        val rvAppointments = findViewById<RecyclerView>(R.id.rvAppointments)

        // Setup RecyclerView
        rvAppointments.layoutManager = LinearLayoutManager(this)

        adapter = AppointmentAdapter(
            onCancel = { appointmentId -> confirmCancel(appointmentId) },
            onReschedule = { appointmentId ->
                Toast.makeText(this, "Reschedule ID $appointmentId (Coming soon!)", Toast.LENGTH_SHORT).show()
            }
        )
        rvAppointments.adapter = adapter

        // Observe ViewModel
        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.appointments.collect { appointments ->
                adapter.submitList(appointments)

                // Show empty state if not loading and list is empty
                val isEmpty = appointments.isEmpty() && !viewModel.isLoading.value
                tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    Toast.makeText(this@MyAppointmentsActivity, errorMsg, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    // Standard Android Confirmation Dialog (Matches React's window.confirm)
    private fun confirmCancel(id: String) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment?")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                viewModel.cancelAppointment(id)
            }
            .setNegativeButton("Go Back", null)
            .show()
    }
}