package edu.cit.ochavillo.schedease

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import edu.cit.ochavillo.schedease.network.AppointmentResponse
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()
    // 🚨 We can reuse your AppointmentAdapter from the previous step!
    private lateinit var adapter: AppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        HeaderManager(this).setupHeader(R.id.mainToolbar)

        val rvAppointments = findViewById<RecyclerView>(R.id.rvAppointments)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        // Tabs
        val btnTabAll = findViewById<MaterialButton>(R.id.btnTabAll)
        val btnTabUpcoming = findViewById<MaterialButton>(R.id.btnTabUpcoming)
        val btnTabCompleted = findViewById<MaterialButton>(R.id.btnTabCompleted)
        val btnTabCancelled = findViewById<MaterialButton>(R.id.btnTabCancelled)

        // Setup RecyclerView & Adapter
        rvAppointments.layoutManager = LinearLayoutManager(this)

        adapter = AppointmentAdapter(
            onCancel = { aptId -> confirmCancel(aptId) },
            onReschedule = { aptId -> openRescheduleDialog(aptId) }
        )
        // 🚨 Note: To add the "Details" click, you'd update AppointmentAdapter to handle an onClick on the card itself
        rvAppointments.adapter = adapter

        // Setup Search
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearch(s.toString())
            }
        })

        // Setup Tabs
        val tabs = listOf(btnTabAll, btnTabUpcoming, btnTabCompleted, btnTabCancelled)
        tabs.forEach { button ->
            button.setOnClickListener {
                viewModel.updateTab(button.text.toString())
                // Reset styles
                tabs.forEach { it.setBackgroundColor(Color.TRANSPARENT); it.setTextColor(Color.GRAY) }
                // Active style
                button.setBackgroundColor(Color.parseColor("#2563EB"))
                button.setTextColor(Color.WHITE)
            }
        }

        // Observe Data
        lifecycleScope.launch {
            viewModel.filteredAppointments.collect { apts ->
                adapter.submitList(apts)
            }
        }

        lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                findViewById<TextView>(R.id.tvUpcomingCount).text = stats.upcoming.toString()
                findViewById<TextView>(R.id.tvPendingCount).text = stats.pending.toString()
                findViewById<TextView>(R.id.tvCompletedCount).text = stats.completed.toString()
                findViewById<TextView>(R.id.tvCancelledCount).text = stats.cancelled.toString()
            }
        }
    }

    private fun confirmCancel(id: String) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.cancelAppointment(id,
                    onSuccess = { Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show() },
                    onError = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
                )
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun openRescheduleDialog(appointmentId: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_reschedule)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val calendarView = dialog.findViewById<CalendarView>(R.id.rescheduleCalendar)
        val rvTimes = dialog.findViewById<RecyclerView>(R.id.rvRescheduleTimes)
        val btnConfirm = dialog.findViewById<MaterialButton>(R.id.btnConfirmReschedule)

        // 🚨 Reuse your TimeSlotAdapter from the booking flow!
        var selectedTime: String? = null
        val timeAdapter = TimeSlotAdapter { time ->
            selectedTime = time
            btnConfirm.isEnabled = true
        }
        rvTimes.layoutManager = GridLayoutManager(this, 3)
        rvTimes.adapter = timeAdapter

        var selectedDate = LocalDate.now()
        calendarView.minDate = System.currentTimeMillis()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            selectedTime = null
            btnConfirm.isEnabled = false
            // You need to pass the establishmentId here! Assuming you retrieve it from the appointment
            viewModel.fetchAvailableTimes("1", selectedDate)
        }

        lifecycleScope.launch {
            viewModel.availableTimes.collect { times ->
                timeAdapter.submitList(times)
            }
        }

        btnConfirm.setOnClickListener {
            selectedTime?.let { time ->
                viewModel.submitReschedule(appointmentId, selectedDate, time,
                    onSuccess = {
                        Toast.makeText(this, "Rescheduled!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    },
                    onError = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
                )
            }
        }

        dialog.show()
    }
}