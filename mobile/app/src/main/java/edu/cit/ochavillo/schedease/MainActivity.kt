package edu.cit.ochavillo.schedease

import android.content.Context
import android.content.Intent // Make sure this is imported!
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.AuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        // 1. LOGIN BUTTON CLICK
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            performLogin(username, password)
        }

        // 2. SIGN UP TEXT CLICK -> OPEN THE NEW SCREEN!
        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // 3. PURE LOGIN LOGIC
    private fun performLogin(user: String, pass: String) {
        if (user.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show()
            return
        }

        val request = AuthRequest(user, pass)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 🚨 We ONLY call login() here now!
                val response = ApiClient.apiService.login(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val token = response.body()?.token
                        saveToken(token)
                        Toast.makeText(this@MainActivity, "Success! Token saved.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Connection Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveToken(token: String?) {
        if (token == null) return
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("JWT_TOKEN", token)
            apply()
        }
    }
}