package edu.cit.ochavillo.schedease

import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.RegisterRequest
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etFirstName = findViewById<EditText>(R.id.etRegFirstName)
        val etLastName = findViewById<EditText>(R.id.etRegLastName)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)
        val tvSignIn = findViewById<TextView>(R.id.tvSignIn)

        // Close this screen and go back to Login when "Sign in" is clicked
        tvSignIn.setOnClickListener {
            finish()
        }

        btnCreateAccount.setOnClickListener {
            val request = RegisterRequest(
                username = etUsername.text.toString().trim(),
                firstName = etFirstName.text.toString().trim(),
                lastName = etLastName.text.toString().trim(),
                email = etEmail.text.toString().trim(),
                password = etPassword.text.toString()
            )

            // Basic validation
            if (request.username.isBlank() || request.password.isBlank() || request.email.isBlank()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Launch the API Call
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiClient.apiService.register(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RegisterActivity, "Account Created! Please Sign In.", Toast.LENGTH_LONG).show()
                            finish() // Closes registration and takes them to Login screen
                        } else {
                            Toast.makeText(this@RegisterActivity, "Registration Failed: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}