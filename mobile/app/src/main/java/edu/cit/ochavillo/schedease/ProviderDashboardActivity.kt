package edu.cit.ochavillo.schedease

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ProviderDashboardActivity : AppCompatActivity() {

    private val viewModel: ProviderDashboardViewModel by viewModels()
    private lateinit var adapter: RecentActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_dashboard)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        TopBarManager(this, drawerLayout).setupTopBar()

        // 2. Setup the actual Sidebar Drawer content
        SidebarManager(this, drawerLayout).setupSidebar(
            navViewId = R.id.sidebarNavView,
            footerId = R.id.sidebarProfileFooter,
            brandHeaderId = R.id.sidebarBrandHeader
        )


        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val layoutContent = findViewById<LinearLayout>(R.id.layoutContent)
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvNoActivity = findViewById<TextView>(R.id.tvNoActivity)
        val rvRecentActivity = findViewById<RecyclerView>(R.id.rvRecentActivity)

        val tvTotalCount = findViewById<TextView>(R.id.tvTotalCount)
        val tvPendingCount = findViewById<TextView>(R.id.tvPendingCount)
        val tvConfirmedCount = findViewById<TextView>(R.id.tvConfirmedCount)
        val tvClientsCount = findViewById<TextView>(R.id.tvClientsCount)

        // Setup RecyclerView
        rvRecentActivity.layoutManager = LinearLayoutManager(this)
        adapter = RecentActivityAdapter()
        rvRecentActivity.adapter = adapter

        // Observe Loading State
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
                if (isLoading) {
                    layoutContent.visibility = View.GONE
                    tvError.visibility = View.GONE
                } else if (viewModel.error.value == null) {
                    layoutContent.visibility = View.VISIBLE
                }
            }
        }

        // Observe Error State
        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    tvError.text = errorMsg
                    tvError.visibility = View.VISIBLE
                    layoutContent.visibility = View.GONE
                }
            }
        }

        // Observe Stats
        lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                tvTotalCount.text = stats.total.toString()
                tvPendingCount.text = stats.pending.toString()
                tvConfirmedCount.text = stats.confirmed.toString()
                tvClientsCount.text = stats.clients.toString()
            }
        }

        // Observe Recent Activity
        lifecycleScope.launch {
            viewModel.recentActivity.collect { activities ->
                adapter.submitList(activities)

                if (activities.isEmpty() && !viewModel.isLoading.value) {
                    tvNoActivity.visibility = View.VISIBLE
                    rvRecentActivity.visibility = View.GONE
                } else {
                    tvNoActivity.visibility = View.GONE
                    rvRecentActivity.visibility = View.VISIBLE
                }
            }
        }
    }
}