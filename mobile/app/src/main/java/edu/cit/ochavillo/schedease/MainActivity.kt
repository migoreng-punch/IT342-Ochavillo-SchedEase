package edu.cit.ochavillo.schedease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.AuthRequest
import edu.cit.ochavillo.schedease.network.SessionManager // 🚨 Import the SessionManager!
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

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            performLogin(username, password)
        }

        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(user: String, pass: String) {
        if (user.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show()
            return
        }

        val request = AuthRequest(user, pass)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.login(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        // 1. Get the token
                        val token = response.body()!!.token

                        // 2. 🚨 Pass it to SessionManager (This decodes the JWT and saves the user state!)
                        SessionManager.processAndSetToken(token)

                        Toast.makeText(this@MainActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                        // 3. 🚨 Navigate to your Home Screen
                        // (Change 'HomeActivity' to whatever your actual dashboard activity is named)
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)

                        // 4. Destroy the Login screen so the user can't press the back button into it
                        finish()

                    } else {
                        Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Connection Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 🚨 You can delete the old saveToken() SharedPreferences function entirely!
}