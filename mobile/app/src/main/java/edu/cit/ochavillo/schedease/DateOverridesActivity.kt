package edu.cit.ochavillo.schedease

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import edu.cit.ochavillo.schedease.network.SessionManager
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DateOverridesActivity : AppCompatActivity() {

    private val viewModel: DateOverridesViewModel by viewModels()
    private lateinit var adapter: OverrideAdapter

    // Form State
    private var selectedDate: LocalDate? = null
    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_overrides)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        // Initialize the Top Bar
        TopBarManager(this, drawerLayout).setupTopBar()

        // Initialize the Sidebar
        SidebarManager(this, drawerLayout).setupSidebar(
            navViewId = R.id.sidebarNavView,
            footerId = R.id.sidebarProfileFooter,
            brandHeaderId = R.id.sidebarBrandHeader
        )

        // View References
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvUnverifiedWarning = findViewById<TextView>(R.id.tvUnverifiedWarning)
        val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)
        val rvOverrides = findViewById<RecyclerView>(R.id.rvOverrides)

        // Form References
        val tvSelectDate = findViewById<TextView>(R.id.tvSelectDate)
        val switchUnavailable = findViewById<SwitchMaterial>(R.id.switchUnavailable)
        val layoutTimes = findViewById<LinearLayout>(R.id.layoutTimes)
        val tvSelectStartTime = findViewById<TextView>(R.id.tvSelectStartTime)
        val tvSelectEndTime = findViewById<TextView>(R.id.tvSelectEndTime)
        val btnAddOverride = findViewById<MaterialButton>(R.id.btnAddOverride)

        // 🚨 Verify User Status for Soft Blocking
        val isVerified = SessionManager.currentUser.value?.isEmailVerified == true
        if (!isVerified) {
            tvUnverifiedWarning.visibility = View.VISIBLE
            tvSelectDate.isEnabled = false
            switchUnavailable.isEnabled = false
            btnAddOverride.isEnabled = false
            btnAddOverride.setBackgroundColor(Color.parseColor("#E5E7EB"))
            btnAddOverride.setTextColor(Color.parseColor("#9CA3AF"))
        }

        // Setup Adapter
        rvOverrides.layoutManager = LinearLayoutManager(this)
        adapter = OverrideAdapter { id ->
            viewModel.deleteOverride(id)
        }
        rvOverrides.adapter = adapter

        // --- Form Interactions ---

        switchUnavailable.setOnCheckedChangeListener { _, isChecked ->
            layoutTimes.visibility = if (isChecked) View.GONE else View.VISIBLE
            validateForm(btnAddOverride, isVerified)
        }

        tvSelectDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Override Date")
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                // Convert timestamp to LocalDate safely
                selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                tvSelectDate.text = selectedDate?.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
                tvSelectDate.setTextColor(Color.parseColor("#111827"))
                validateForm(btnAddOverride, isVerified)
            }
            picker.show(supportFragmentManager, "DATE_PICKER")
        }

        tvSelectStartTime.setOnClickListener { showTimePicker(true, tvSelectStartTime, btnAddOverride, isVerified) }
        tvSelectEndTime.setOnClickListener { showTimePicker(false, tvSelectEndTime, btnAddOverride, isVerified) }

        btnAddOverride.setOnClickListener {
            selectedDate?.let { date ->
                viewModel.addOverride(
                    date = date.toString(),
                    isUnavailable = switchUnavailable.isChecked,
                    startTime = selectedStartTime,
                    endTime = selectedEndTime
                )
            }
        }

        // --- ViewModel Observers ---

        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isSubmitting.collect { submitting ->
                if (isVerified) {
                    btnAddOverride.text = if (submitting) "Adding..." else "Add Override"
                    validateForm(btnAddOverride, isVerified) // Re-check constraints
                }
            }
        }

        lifecycleScope.launch {
            viewModel.overrides.collect { list ->
                adapter.submitList(list)

                val isEmpty = list.isEmpty() && !viewModel.isLoading.value
                tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvOverrides.visibility = if (isEmpty) View.GONE else View.VISIBLE
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
            viewModel.addSuccess.collect { success ->
                if (success) {
                    Toast.makeText(this@DateOverridesActivity, "Override Added!", Toast.LENGTH_SHORT).show()

                    // Reset Form
                    selectedDate = null
                    selectedStartTime = null
                    selectedEndTime = null
                    tvSelectDate.text = "Select Date"
                    tvSelectDate.setTextColor(Color.parseColor("#9CA3AF"))
                    tvSelectStartTime.text = "--:--"
                    tvSelectEndTime.text = "--:--"
                    switchUnavailable.isChecked = true
                    validateForm(btnAddOverride, isVerified)

                    viewModel.clearMessages()
                }
            }
        }
    }

    private fun showTimePicker(isStartTime: Boolean, tv: TextView, btn: MaterialButton, isVerified: Boolean) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(9)
            .setMinute(0)
            .setTitleText(if (isStartTime) "Select Start Time" else "Select End Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val formattedTime = String.format("%02d:%02d", picker.hour, picker.minute)
            if (isStartTime) selectedStartTime = formattedTime else selectedEndTime = formattedTime
            tv.text = formattedTime
            validateForm(btn, isVerified)
        }
        picker.show(supportFragmentManager, "TIME_PICKER")
    }

    // Ensures the button is only clickable when the required fields are filled
    private fun validateForm(btn: MaterialButton, isVerified: Boolean) {
        if (!isVerified || viewModel.isSubmitting.value) {
            btn.isEnabled = false
            return
        }

        val hasDate = selectedDate != null
        val switchChecked = findViewById<SwitchMaterial>(R.id.switchUnavailable).isChecked
        val hasTimes = selectedStartTime != null && selectedEndTime != null

        btn.isEnabled = hasDate && (switchChecked || hasTimes)
    }
}