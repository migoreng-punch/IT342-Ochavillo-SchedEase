// SessionManager.kt
package edu.cit.ochavillo.schedease.network

import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

object SessionManager {

    // Equivalent to const [user, setUser] = useState(null)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Equivalent to const [accessToken, setAccessToken] = useState(null)
    var accessToken: String? = null
        private set

    // Equivalent to const [isAuthReady, setIsAuthReady] = useState(false)
    private val _isAuthReady = MutableStateFlow(false)
    val isAuthReady: StateFlow<Boolean> = _isAuthReady.asStateFlow()

    // 1. Helper function to decode and set everything synchronously
    fun processAndSetToken(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size == 3) {
                // Decode the payload (middle part of the JWT)
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val json = JSONObject(payload)

                val user = User(
                    username = json.optString("sub", ""),
                    firstName = json.optString("firstname", "User"),
                    role = json.optString("role", "CLIENT"),
                    isEmailVerified = json.optBoolean("isEmailVerified", false)
                )

                this.accessToken = token
                _currentUser.value = user
            }
        } catch (e: Exception) {
            e.printStackTrace()
            clearSession()
        }
    }

    // 2. AUTO LOGIN (No API Call - Just sets state)
    fun autoLogin(token: String) {
        processAndSetToken(token)
    }

    // Clears state for logout or failed refresh
    fun clearSession() {
        accessToken = null
        _currentUser.value = null
    }

    // Mark auth as checked (used after splash screen / refresh attempt)
    fun setAuthReady() {
        _isAuthReady.value = true
    }
}