package edu.cit.ochavillo.schedease

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import edu.cit.ochavillo.schedease.network.SessionManager
import kotlinx.coroutines.launch

class ProviderSettingsActivity : AppCompatActivity() {

    private val viewModel: ProviderSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_settings)

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
        val layoutForm = findViewById<LinearLayout>(R.id.layoutForm)
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvUnverifiedWarning = findViewById<TextView>(R.id.tvUnverifiedWarning)

        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val btnDelete = findViewById<MaterialButton>(R.id.btnDelete)

        // Form Fields
        val etEstName = findViewById<EditText>(R.id.etEstName)
        val etDesc = findViewById<EditText>(R.id.etDesc)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etDuration = findViewById<EditText>(R.id.etDuration)
        val etBuffer = findViewById<EditText>(R.id.etBuffer)
        val etCutoff = findViewById<EditText>(R.id.etCutoff)

        // 🚨 Verify User Status for Soft Blocking
        val isVerified = SessionManager.currentUser.value?.isEmailVerified == true
        if (!isVerified) {
            tvUnverifiedWarning.visibility = View.VISIBLE
            btnSave.isEnabled = false
            btnSave.setBackgroundColor(Color.parseColor("#E5E7EB"))
            btnSave.setTextColor(Color.parseColor("#9CA3AF"))
        }

        // --- View Model Observers ---

        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
                layoutForm.visibility = if (loading) View.GONE else View.VISIBLE
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
            // Populate form once when data is loaded
            viewModel.formData.collect { data ->
                if (data != null) {
                    etEstName.setText(data.establishmentName)
                    etDesc.setText(data.description)
                    etAddress.setText(data.address)
                    etEmail.setText(data.contactEmail)
                    etPhone.setText(data.contactPhone)
                    etDuration.setText(data.slotDurationMinutes.toString())
                    etBuffer.setText(data.bufferMinutes.toString())
                    etCutoff.setText(data.bookingCutoffHours.toString())
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    tvError.text = errorMsg
                    tvError.visibility = View.VISIBLE
                    viewModel.resetState()
                } else {
                    tvError.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.saveSuccess.collect { success ->
                if (success) {
                    Toast.makeText(this@ProviderSettingsActivity, "Settings updated successfully!", Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.deleteSuccess.collect { success ->
                if (success) {
                    Toast.makeText(this@ProviderSettingsActivity, "Establishment Deleted", Toast.LENGTH_SHORT).show()
                    // Redirect back to Home/Dashboard
                    val intent = Intent(this@ProviderSettingsActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }

        // --- Click Listeners ---

        btnSave.setOnClickListener {
            // Re-grab the current User details so we don't overwrite them with empty strings
            val currentUserData = viewModel.formData.value

            val updatedData = SettingsFormData(
                establishmentName = etEstName.text.toString(),
                description = etDesc.text.toString(),
                address = etAddress.text.toString(),
                contactEmail = etEmail.text.toString(),
                contactPhone = etPhone.text.toString(),
                slotDurationMinutes = etDuration.text.toString().toIntOrNull() ?: 0,
                bufferMinutes = etBuffer.text.toString().toIntOrNull() ?: 0,
                bookingCutoffHours = etCutoff.text.toString().toIntOrNull() ?: 0,
                // Pass existing user data back
                username = currentUserData?.username ?: "",
                firstName = currentUserData?.firstName ?: "",
                lastName = currentUserData?.lastName ?: ""
            )
            viewModel.saveSettings(updatedData)
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    // Android's native alert dialog representing your Danger Modal
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Establishment?")
            .setMessage("Are you absolutely sure? This will permanently delete your establishment, schedule, and all appointments. This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteEstablishment()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}