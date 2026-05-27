package edu.cit.ochavillo.schedease

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import edu.cit.ochavillo.schedease.network.SessionManager
import kotlinx.coroutines.launch

class SecuritySettingsActivity : AppCompatActivity() {

    private val viewModel: SecuritySettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_settings)

        HeaderManager(this).setupHeader(R.id.mainToolbar)

        val tvFeedback = findViewById<TextView>(R.id.tvFeedback)
        val etCurrentPassword = findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)

        val btnUpdatePassword = findViewById<MaterialButton>(R.id.btnUpdatePassword)
        val btnDeleteAccount = findViewById<MaterialButton>(R.id.btnDeleteAccount)

        // --- Event Listeners ---

        btnUpdatePassword.setOnClickListener {
            viewModel.changePassword(
                current = etCurrentPassword.text.toString(),
                newPass = etNewPassword.text.toString(),
                confirm = etConfirmPassword.text.toString()
            )
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // --- ViewModel Observers ---

        lifecycleScope.launch {
            viewModel.isChangingPassword.collect { changing ->
                btnUpdatePassword.isEnabled = !changing
                btnUpdatePassword.text = if (changing) "Updating..." else "Update password"
            }
        }

        lifecycleScope.launch {
            viewModel.feedbackMessage.collect { feedback ->
                if (feedback != null) {
                    tvFeedback.visibility = View.VISIBLE
                    tvFeedback.text = feedback.first

                    if (feedback.second) { // Success
                        tvFeedback.setTextColor(Color.parseColor("#065F46"))
                        tvFeedback.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#D1FAE5"))

                        // Clear inputs on success
                        etCurrentPassword.setText("")
                        etNewPassword.setText("")
                        etConfirmPassword.setText("")

                        // Hide after 3 seconds
                        tvFeedback.postDelayed({ viewModel.clearFeedback() }, 3000)
                    } else { // Error
                        tvFeedback.setTextColor(Color.parseColor("#DC2626"))
                        tvFeedback.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                    }
                } else {
                    tvFeedback.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.deleteSuccess.collect { success ->
                if (success) {
                    Toast.makeText(this@SecuritySettingsActivity, "Account Deleted", Toast.LENGTH_SHORT).show()

                    // Clear Session & Redirect to Login
                    SessionManager.clearSession()
                    val intent = Intent(this@SecuritySettingsActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    // Android's native alert dialog replacing the React Modal
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Your Account?")
            .setMessage("Are you absolutely sure you want to delete your account? This action is permanent and cannot be undone. All of your profile data, appointments, and settings will be lost forever.")
            .setPositiveButton("Permanently Delete") { _, _ ->
                viewModel.deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}