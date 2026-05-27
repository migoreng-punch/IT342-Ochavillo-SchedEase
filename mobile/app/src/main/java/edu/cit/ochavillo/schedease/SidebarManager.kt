package edu.cit.ochavillo.schedease

import android.content.Intent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import edu.cit.ochavillo.schedease.network.SessionManager

class SidebarManager(
    private val activity: AppCompatActivity,
    private val drawerLayout: DrawerLayout
) {

    fun setupSidebar(navViewId: Int, footerId: Int, brandHeaderId: Int) {
        val navView = activity.findViewById<NavigationView>(navViewId)
        val profileFooter = activity.findViewById<LinearLayout>(footerId)
        val brandHeader = activity.findViewById<LinearLayout>(brandHeaderId)
        val user = SessionManager.currentUser.value

        // Set Avatar and Username
        if (user != null) {
            val tvAvatar = activity.findViewById<TextView>(R.id.tvSidebarAvatar)
            val tvUsername = activity.findViewById<TextView>(R.id.tvSidebarUsername)

            tvAvatar.text = user.username.firstOrNull()?.uppercase() ?: "U"
            tvUsername.text = user.username
        }

        // 1. Handle Brand Click
        brandHeader.setOnClickListener {
            closeDrawer()
            navigateTo(HomeActivity::class.java)
        }

        // 2. Handle Main Navigation Links
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_overview -> navigateTo(ProviderDashboardActivity::class.java)
                R.id.nav_appointments -> navigateTo(ProviderAppointmentsActivity::class.java)
                R.id.nav_schedule -> navigateTo(ProviderScheduleActivity::class.java)
                R.id.nav_overrides -> navigateTo(DateOverridesActivity::class.java)
                R.id.nav_settings -> navigateTo(ProviderSettingsActivity::class.java)
            }
            closeDrawer()
            true
        }

        // 3. Handle Profile Dropup (Footer Click)
        profileFooter.setOnClickListener { view ->
            val popup = PopupMenu(activity, view)
            popup.menuInflater.inflate(R.menu.menu_provider, popup.menu)

            // Hide Provider-specific items if user isn't a provider
            if (user?.role != "PROVIDER") {
                popup.menu.findItem(R.id.nav_establishment).isVisible = false
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_profile -> navigateTo(AccountSettingsActivity::class.java) // Adjust if you have a specific Profile view
                    R.id.nav_appointments -> navigateTo(MyAppointmentsActivity::class.java)
                    R.id.nav_dashboard -> navigateTo(DashboardActivity::class.java)
                    R.id.nav_establishment -> navigateTo(ProviderDashboardActivity::class.java)
                    R.id.nav_settings -> navigateTo(SecuritySettingsActivity::class.java)
                    R.id.nav_logout -> showLogoutDialog()
                }
                closeDrawer()
                true
            }
            popup.show()
        }
    }

    private fun navigateTo(destination: Class<*>) {
        // Prevent launching the activity if we are already on it
        if (activity.javaClass == destination) return

        val intent = Intent(activity, destination)
        // Keep the back stack clean
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        activity.startActivity(intent)
    }

    private fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Ready to leave?")
            .setMessage("Are you sure you want to log out of your account? You will need to log back in to manage your appointments.")
            .setPositiveButton("Log Out") { _, _ ->
                SessionManager.clearSession()
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(intent)
                activity.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}