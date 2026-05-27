package edu.cit.ochavillo.schedease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SetupEstablishmentActivity : AppCompatActivity() {

    private val viewModel: SetupEstablishmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_establishment)

        HeaderManager(this).setupHeader(R.id.mainToolbar)

        val tvError = findViewById<TextView>(R.id.tvError)
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etDuration = findViewById<EditText>(R.id.etDuration)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnSubmit = findViewById<MaterialButton>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            viewModel.createEstablishment(
                name = etName.text.toString(),
                email = etEmail.text.toString(),
                durationStr = etDuration.text.toString(),
                address = etAddress.text.toString(),
                description = etDescription.text.toString()
            )
        }

        // --- ViewModel Observers ---

        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                btnSubmit.isEnabled = !loading
                btnSubmit.text = if (loading) "Setting up..." else "Complete Setup"
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                if (errorMsg != null) {
                    tvError.visibility = View.VISIBLE
                    tvError.text = errorMsg
                    viewModel.clearError() // Reset after showing
                } else {
                    tvError.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.success.collect { success ->
                if (success) {
                    Toast.makeText(this@SetupEstablishmentActivity, "Establishment Created!", Toast.LENGTH_SHORT).show()

                    // Route to Provider Dashboard
                    val intent = Intent(this@SetupEstablishmentActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}