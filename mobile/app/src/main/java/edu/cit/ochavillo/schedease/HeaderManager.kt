package edu.cit.ochavillo.schedease

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.SessionManager
import kotlinx.coroutines.launch

class HeaderManager(private val activity: AppCompatActivity) {

    fun setupHeader(toolbarId: Int) {
        val toolbar = activity.findViewById<MaterialToolbar>(toolbarId) ?: return
        val user = SessionManager.currentUser.value

        val btnAvatar = toolbar.findViewById<MaterialCardView>(R.id.btnAvatar)
        val tvAvatarLetter = toolbar.findViewById<TextView>(R.id.tvAvatarLetter)
        val btnCreateEstablishment = toolbar.findViewById<MaterialButton>(R.id.btnCreateEstablishment)
        val layoutLogo = toolbar.findViewById<LinearLayout>(R.id.layoutLogo)

        // 1. Setup Logo Click
        layoutLogo.setOnClickListener {
            val destination = if (user != null) HomeActivity::class.java else MainActivity::class.java
            val intent = Intent(activity, destination)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
        }

        // 2. Setup Avatar Letter
        if (user != null) {
            tvAvatarLetter.text = user.username.firstOrNull()?.uppercase() ?: "U"
        } else {
            btnAvatar.visibility = View.GONE // Hide avatar if not logged in
        }

        // 3. Setup "Create Establishment" Button (Providers Only)
        if (user?.role == "PROVIDER") {
            checkEstablishmentStatus(btnCreateEstablishment, user.isEmailVerified)
        }

        // 4. Setup Dropdown Menu
        btnAvatar.setOnClickListener {
            showDropdownMenu(it, user?.role)
        }
    }

    private fun checkEstablishmentStatus(btnCreate: MaterialButton, isVerified: Boolean) {
        activity.lifecycleScope.launch {
            try {
                // Mimics the React useEffect background check
                val response = ApiClient.apiService.getMyEstablishment()
                if (!response.isSuccessful) {
                    // No establishment found, show the button!
                    btnCreate.visibility = View.VISIBLE

                    if (isVerified) {
                        btnCreate.isEnabled = true
                        btnCreate.setOnClickListener {
                            activity.startActivity(Intent(activity, SetupEstablishmentActivity::class.java))
                        }
                    } else {
                        // Soft Block for Unverified
                        btnCreate.isEnabled = false
                        btnCreate.setBackgroundColor(Color.parseColor("#F3F4F6")) // Gray-100
                        btnCreate.setTextColor(Color.parseColor("#9CA3AF")) // Gray-400
                        btnCreate.setOnClickListener {
                            Toast.makeText(activity, "You need to verify your email to access this.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                // If it fails, assume no establishment so they can try to create one
                btnCreate.visibility = View.VISIBLE
            }
        }
    }

    private fun showDropdownMenu(anchorView: View, role: String?) {
        val popup = PopupMenu(activity, anchorView)

        // Inflate the correct menu based on role
        if (role == "PROVIDER") {
            popup.menuInflater.inflate(R.menu.menu_provider, popup.menu)
        } else {
            popup.menuInflater.inflate(R.menu.menu_client, popup.menu)
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> activity.startActivity(Intent(activity, AccountSettingsActivity::class.java))
                R.id.nav_appointments -> activity.startActivity(Intent(activity, MyAppointmentsActivity::class.java)) // Or ProviderAppointments based on role
                R.id.nav_dashboard -> activity.startActivity(Intent(activity, DashboardActivity::class.java))
                R.id.nav_establishment -> activity.startActivity(Intent(activity, ProviderDashboardActivity::class.java))
                R.id.nav_settings -> activity.startActivity(Intent(activity, SecuritySettingsActivity::class.java))
                R.id.nav_logout -> showLogoutDialog()
            }
            true
        }
        popup.show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Ready to leave?")
            .setMessage("Are you sure you want to log out of your account? You will need to log back in to manage your appointments.")
            .setPositiveButton("Log Out") { _, _ ->
                // Clear session and navigate to Login
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