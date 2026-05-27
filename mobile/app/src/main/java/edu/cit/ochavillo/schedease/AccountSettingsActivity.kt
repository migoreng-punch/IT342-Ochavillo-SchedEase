package edu.cit.ochavillo.schedease

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class AccountSettingsActivity : AppCompatActivity() {

    private val viewModel: AccountSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        HeaderManager(this).setupHeader(R.id.mainToolbar)

        // Basic Views
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val layoutContent = findViewById<LinearLayout>(R.id.layoutContent)
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvSuccess = findViewById<TextView>(R.id.tvSuccess)

        // Sidebar Profile Views
        val tvInitials = findViewById<TextView>(R.id.tvInitials)
        val tvFullName = findViewById<TextView>(R.id.tvFullName)
        val tvUsernameHandle = findViewById<TextView>(R.id.tvUsernameHandle)
        val tvUnverifiedBadge = findViewById<TextView>(R.id.tvUnverifiedBadge)

        // Sidebar Stats Views
        val tvStatTotal = findViewById<TextView>(R.id.tvStatTotal)
        val tvStatCompleted = findViewById<TextView>(R.id.tvStatCompleted)
        val tvStatUpcoming = findViewById<TextView>(R.id.tvStatUpcoming)

        // Form Views
        val layoutResendBanner = findViewById<LinearLayout>(R.id.layoutResendBanner)
        val btnResendVerification = findViewById<TextView>(R.id.btnResendVerification)
        val tvResendFeedback = findViewById<TextView>(R.id.tvResendFeedback)

        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        // Underline the resend text manually like CSS underline
        btnResendVerification.paintFlags = btnResendVerification.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // --- Event Listeners ---

        btnSave.setOnClickListener {
            viewModel.updateProfile(
                firstName = etFirstName.text.toString(),
                lastName = etLastName.text.toString(),
                username = etUsername.text.toString()
            )
        }

        btnResendVerification.setOnClickListener {
            viewModel.resendVerification()
        }

        // --- ViewModel Observers ---

        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
                layoutContent.visibility = if (loading) View.GONE else View.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.isSaving.collect { saving ->
                btnSave.isEnabled = !saving
                btnSave.text = if (saving) "Saving..." else "Save Changes"
            }
        }

        lifecycleScope.launch {
            viewModel.isResending.collect { resending ->
                btnResendVerification.isEnabled = !resending
                btnResendVerification.text = if (resending) "Sending..." else "Resend verification email"
            }
        }

        lifecycleScope.launch {
            viewModel.userProfile.collect { profile ->
                if (profile != null) {
                    // Update Sidebar
                    val firstI = profile.firstName.firstOrNull()?.uppercase() ?: ""
                    val lastI = profile.lastName.firstOrNull()?.uppercase() ?: ""
                    tvInitials.text = "$firstI$lastI"

                    tvFullName.text = "${profile.firstName} ${profile.lastName}"
                    tvUsernameHandle.text = "@${profile.username}"

                    val isUnverified = profile.enabled == false
                    tvUnverifiedBadge.visibility = if (isUnverified) View.VISIBLE else View.GONE
                    layoutResendBanner.visibility = if (isUnverified) View.VISIBLE else View.GONE

                    // Update Form Inputs (Only if they are empty to avoid overwriting user typing)
                    if (etFirstName.text.isEmpty()) etFirstName.setText(profile.firstName)
                    if (etLastName.text.isEmpty()) etLastName.setText(profile.lastName)
                    if (etUsername.text.isEmpty()) etUsername.setText(profile.username)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.activityStats.collect { stats ->
                tvStatTotal.text = stats.total.toString()
                tvStatCompleted.text = stats.completed.toString()
                tvStatUpcoming.text = stats.upcoming.toString()
            }
        }

        // Message Handling (Errors, Success, Resend Feedback)
        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                tvError.visibility = if (errorMsg != null) View.VISIBLE else View.GONE
                tvError.text = errorMsg ?: ""
            }
        }

        lifecycleScope.launch {
            viewModel.successMessage.collect { msg ->
                tvSuccess.visibility = if (msg != null) View.VISIBLE else View.GONE
                tvSuccess.text = msg ?: ""
                if (msg != null) {
                    // Auto-hide success message after 3 seconds like React
                    tvSuccess.postDelayed({ viewModel.clearMessages() }, 3000)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.resendMessage.collect { msg ->
                if (msg != null) {
                    tvResendFeedback.visibility = View.VISIBLE
                    tvResendFeedback.text = msg
                    if (msg.contains("success", true)) {
                        tvResendFeedback.setTextColor(Color.parseColor("#15803D")) // Green
                    } else {
                        tvResendFeedback.setTextColor(Color.parseColor("#DC2626")) // Red
                    }
                } else {
                    tvResendFeedback.visibility = View.GONE
                }
            }
        }
    }
}