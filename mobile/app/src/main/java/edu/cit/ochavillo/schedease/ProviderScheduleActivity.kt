package edu.cit.ochavillo.schedease

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import edu.cit.ochavillo.schedease.network.SessionManager
import kotlinx.coroutines.launch

class ProviderScheduleActivity : AppCompatActivity() {

    private val viewModel: ProviderScheduleViewModel by viewModels()
    private lateinit var adapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_schedule)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        // 1. 🚨 Setup the New Top Bar with the Hamburger Icon
        TopBarManager(this, drawerLayout).setupTopBar()

        // 2. Setup the actual Sidebar Drawer content
        SidebarManager(this, drawerLayout).setupSidebar(
            navViewId = R.id.sidebarNavView,
            footerId = R.id.sidebarProfileFooter,
            brandHeaderId = R.id.sidebarBrandHeader
        )

        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val rvSchedule = findViewById<RecyclerView>(R.id.rvSchedule)
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvUnverifiedWarning = findViewById<TextView>(R.id.tvUnverifiedWarning)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        // 🚨 Soft-Block Logic from AuthContext
        val isVerified = SessionManager.currentUser.value?.isEmailVerified == true
        if (!isVerified) {
            tvUnverifiedWarning.visibility = View.VISIBLE
            btnSave.isEnabled = false
            btnSave.setBackgroundColor(android.graphics.Color.parseColor("#E5E7EB"))
            btnSave.setTextColor(android.graphics.Color.parseColor("#9CA3AF"))
        }

        // Setup Adapter
        rvSchedule.layoutManager = LinearLayoutManager(this)
        adapter = ScheduleAdapter(
            onToggleChanged = { dayOfWeek, isWorking ->
                viewModel.updateToggle(dayOfWeek, isWorking)
            },
            onTimeClicked = { dayOfWeek, isStartTime, currentTime ->
                showTimePicker(dayOfWeek, isStartTime, currentTime)
            }
        )
        rvSchedule.adapter = adapter

        btnSave.setOnClickListener {
            viewModel.saveSchedule()
        }

        // --- View Model Observers ---

        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
                rvSchedule.visibility = if (loading) View.GONE else View.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.isSaving.collect { saving ->
                if (isVerified) {
                    btnSave.isEnabled = !saving
                    btnSave.text = if (saving) "Saving..." else "Save Changes"
                }
            }
        }

        lifecycleScope.launch {
            viewModel.schedule.collect { schedule ->
                adapter.submitList(schedule)
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    tvError.text = errorMsg
                    tvError.visibility = View.VISIBLE
                    viewModel.clearMessages()
                } else {
                    tvError.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.saveSuccess.collect { success ->
                if (success == true) {
                    Toast.makeText(this@ProviderScheduleActivity, "Schedule Saved!", Toast.LENGTH_SHORT).show()
                    viewModel.clearMessages()
                }
            }
        }
    }

    // Opens Android's Native Clock UI
    private fun showTimePicker(dayOfWeek: String, isStartTime: Boolean, currentTime: String) {
        var hour = 9
        var minute = 0

        // Parse existing time if it exists (e.g., "14:30")
        if (currentTime.isNotBlank() && currentTime.contains(":")) {
            val parts = currentTime.split(":")
            hour = parts[0].toIntOrNull() ?: 9
            minute = parts[1].toIntOrNull() ?: 0
        }

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H) // Using 24H to match Spring Boot expectations natively
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(if (isStartTime) "Select Start Time" else "Select End Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val formattedTime = String.format("%02d:%02d", picker.hour, picker.minute)
            viewModel.updateTime(dayOfWeek, isStartTime, formattedTime)
        }

        picker.show(supportFragmentManager, "TIME_PICKER")
    }
}