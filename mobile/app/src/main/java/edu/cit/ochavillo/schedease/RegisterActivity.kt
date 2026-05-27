package edu.cit.ochavillo.schedease

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.RegisterRequest
import edu.cit.ochavillo.schedease.network.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private var selectedRole = "CLIENT" // Default role

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toggleRole = findViewById<MaterialButtonToggleGroup>(R.id.toggleRole)
        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etFirstName = findViewById<EditText>(R.id.etRegFirstName)
        val etLastName = findViewById<EditText>(R.id.etRegLastName)
        val etPhoneNumber = findViewById<EditText>(R.id.etRegPhoneNumber)
        val etAddress = findViewById<EditText>(R.id.etRegAddress)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)
        val tvSignIn = findViewById<TextView>(R.id.tvSignIn)

        // --- Role Selection Logic ---
        toggleRole.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedRole = if (checkedId == R.id.btnRoleProvider) "PROVIDER" else "CLIENT"
            }
        }

        // --- Real-time Phone Formatting (Like React's onChange interceptor) ---
        etPhoneNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                isFormatting = true
                // 1. Strip out everything that is NOT a number
                val digitsOnly = s.toString().replace(Regex("\\D"), "")

                // 2. Cap the length at 11 digits (redundant due to XML maxLength, but safe)
                val formatted = if (digitsOnly.length > 11) digitsOnly.substring(0, 11) else digitsOnly

                // 3. Update the input value if it changed
                if (s.toString() != formatted) {
                    etPhoneNumber.setText(formatted)
                    etPhoneNumber.setSelection(formatted.length) // Keep cursor at the end
                }
                isFormatting = false
            }
        })

        // Close this screen and go back to Login when "Sign in" is clicked
        tvSignIn.setOnClickListener {
            finish()
        }

        btnCreateAccount.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val phone = etPhoneNumber.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // --- Comprehensive Validation matching Zod Schema ---
            if (username.length < 3) {
                etUsername.error = "Username must be at least 3 characters"
                return@setOnClickListener
            }
            if (firstName.length < 2) {
                etFirstName.error = "First name is required"
                return@setOnClickListener
            }
            if (lastName.length < 2) {
                etLastName.error = "Last name is required"
                return@setOnClickListener
            }
            if (!phone.matches(Regex("^09\\d{9}$"))) {
                etPhoneNumber.error = "Please enter a valid 11-digit number (e.g., 09123456789)"
                return@setOnClickListener
            }
            if (address.length < 5) {
                etAddress.error = "Please enter your full address"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Invalid email"
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            // --- Build Payload & Launch API Call ---
            val request = RegisterRequest(
                username = username,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                address = address,
                email = email,
                password = password,
                role = selectedRole
            )

            btnCreateAccount.isEnabled = false // Prevent double clicks
            btnCreateAccount.text = "Creating account..."

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiClient.apiService.register(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {

                            // 1. Grab the token returned by your Spring Boot backend
                            val token = response.body()!!.token

                            // 2. 🚨 Auto-Login!
                            SessionManager.processAndSetToken(token)

                            Toast.makeText(this@RegisterActivity, "Account Created!", Toast.LENGTH_SHORT).show()

                            // 3. Navigate straight to the Home Screen
                            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                            startActivity(intent)

                            // 4. finishAffinity() clears the entire back-stack so they can't go back to Register OR Login
                            finishAffinity()

                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Registration failed."
                            Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        btnCreateAccount.isEnabled = true
                        btnCreateAccount.text = "Create Account"
                    }
                }
            }
        }
    }
}