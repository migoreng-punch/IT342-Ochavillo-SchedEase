package edu.cit.ochavillo.schedease

import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import edu.cit.ochavillo.schedease.network.SessionManager
import kotlinx.coroutines.launch
import java.time.LocalDate

class EstablishmentDetailActivity : AppCompatActivity() {

    private val viewModel: EstablishmentDetailViewModel by viewModels()
    private lateinit var adapter: TimeSlotAdapter
    private var establishmentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_establishment_detail)

        HeaderManager(this).setupHeader(R.id.mainToolbar)

        // Catch the ID passed from HomeActivity
        establishmentId = intent.getStringExtra("EST_ID")
        if (establishmentId == null) {
            Toast.makeText(this, "Error loading establishment", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val contentLayout = findViewById<LinearLayout>(R.id.contentLayout)
        val tvEstName = findViewById<TextView>(R.id.tvEstName)
        val tvEstDesc = findViewById<TextView>(R.id.tvEstDesc)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val rvTimeSlots = findViewById<RecyclerView>(R.id.rvTimeSlots)
        val tvNoSlots = findViewById<TextView>(R.id.tvNoSlots)
        val tvUnverifiedWarning = findViewById<TextView>(R.id.tvUnverifiedWarning)
        val btnConfirmBooking = findViewById<MaterialButton>(R.id.btnConfirmBooking)

        // 🚨 Verify User Status for Soft Blocking
        val isVerified = SessionManager.currentUser.value?.isEmailVerified == true
        if (!isVerified) {
            tvUnverifiedWarning.visibility = View.VISIBLE
            // We keep it visible but disabled later in state collection
        }

        // Setup Adapter
        adapter = TimeSlotAdapter { selectedTime ->
            viewModel.selectTime(selectedTime)
            updateButtonState(btnConfirmBooking, isVerified)
        }
        rvTimeSlots.adapter = adapter

        // Setup Calendar
        calendarView.minDate = System.currentTimeMillis() - 1000 // Prevent past dates
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Note: Month is 0-indexed in CalendarView (Jan = 0)
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            viewModel.updateDateAndFetchSlots(establishmentId!!, selectedDate)
            updateButtonState(btnConfirmBooking, isVerified)
        }

        // Setup Booking Button
        btnConfirmBooking.setOnClickListener {
            establishmentId?.let { id ->
                viewModel.bookAppointment(id)
            }
        }

        // --- OBSERVE VIEWMODEL STATE ---

        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
                contentLayout.visibility = if (loading) View.GONE else View.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.establishment.collect { est ->
                tvEstName.text = est?.name ?: ""
                tvEstDesc.text = est?.description ?: ""
            }
        }

        lifecycleScope.launch {
            viewModel.availableSlots.collect { slots ->
                adapter.submitList(slots)
                rvTimeSlots.visibility = if (slots.isNotEmpty()) View.VISIBLE else View.GONE
                tvNoSlots.visibility = if (slots.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isBooking.collect { booking ->
                btnConfirmBooking.text = if (booking) "Confirming..." else "Confirm Booking"
                updateButtonState(btnConfirmBooking, isVerified)
            }
        }

        lifecycleScope.launch {
            viewModel.bookingSuccess.collect { success ->
                if (success == true) {
                    Toast.makeText(this@EstablishmentDetailActivity, "Booking Confirmed!", Toast.LENGTH_LONG).show()
                    finish() // Return to Home
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                if (error != null) {
                    Toast.makeText(this@EstablishmentDetailActivity, error, Toast.LENGTH_LONG).show()
                    viewModel.resetMessages()
                }
            }
        }

        // Initial fetch
        viewModel.fetchEstablishmentAndSlots(establishmentId!!)
    }

    private fun updateButtonState(btn: MaterialButton, isVerified: Boolean) {
        val hasSelectedTime = adapter.getSelectedTime() != null
        val isBooking = viewModel.isBooking.value

        btn.isEnabled = hasSelectedTime && isVerified && !isBooking
    }
}