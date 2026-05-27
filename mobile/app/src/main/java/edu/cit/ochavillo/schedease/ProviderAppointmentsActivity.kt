package edu.cit.ochavillo.schedease

import android.graphics.Color
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
import kotlinx.coroutines.launch

class ProviderAppointmentsActivity : AppCompatActivity() {

    private val viewModel: ProviderAppointmentsViewModel by viewModels()
    private lateinit var adapter: ProviderAppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_appointments)

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
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)
        val rvAppointments = findViewById<RecyclerView>(R.id.rvAppointments)

        // Setup Adapter
        rvAppointments.layoutManager = LinearLayoutManager(this)
        adapter = ProviderAppointmentAdapter { id, newStatus ->
            viewModel.updateStatus(id, newStatus)
            Toast.makeText(this, "Marked as $newStatus", Toast.LENGTH_SHORT).show()
        }
        rvAppointments.adapter = adapter

        // Setup Tabs
        val btnTabAll = findViewById<MaterialButton>(R.id.btnTabAll)
        val btnTabPending = findViewById<MaterialButton>(R.id.btnTabPending)
        val btnTabConfirmed = findViewById<MaterialButton>(R.id.btnTabConfirmed)
        val btnTabCompleted = findViewById<MaterialButton>(R.id.btnTabCompleted)
        val btnTabCancelled = findViewById<MaterialButton>(R.id.btnTabCancelled)

        val tabs = listOf(btnTabAll, btnTabPending, btnTabConfirmed, btnTabCompleted, btnTabCancelled)

        tabs.forEach { button ->
            button.setOnClickListener {
                viewModel.setFilter(button.text.toString().uppercase())

                // Reset styles
                tabs.forEach {
                    it.setBackgroundColor(Color.TRANSPARENT)
                    it.setTextColor(Color.parseColor("#6B7280"))
                }

                // Active style
                button.setBackgroundColor(Color.parseColor("#2563EB"))
                button.setTextColor(Color.WHITE)
            }
        }

        // Observe State
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    tvError.text = errorMsg
                    tvError.visibility = View.VISIBLE
                    viewModel.clearError()
                } else {
                    tvError.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.filteredAppointments.collect { apts ->
                adapter.submitList(apts)

                val isEmpty = apts.isEmpty() && !viewModel.isLoading.value
                tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvAppointments.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }
}