package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.UpdateProfileRequest
import edu.cit.ochavillo.schedease.network.UserProfileDto
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ActivityStats(
    val total: Int = 0,
    val completed: Int = 0,
    val upcoming: Int = 0
)

class AccountSettingsViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfileDto?>(null)
    val userProfile: StateFlow<UserProfileDto?> = _userProfile.asStateFlow()

    private val _activityStats = MutableStateFlow(ActivityStats())
    val activityStats: StateFlow<ActivityStats> = _activityStats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isResending = MutableStateFlow(false)
    val isResending: StateFlow<Boolean> = _isResending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _resendMessage = MutableStateFlow<String?>(null)
    val resendMessage: StateFlow<String?> = _resendMessage.asStateFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch Profile and Appointments in parallel
                val profileDeferred = async { ApiClient.apiService.getProfile() }
                val aptDeferred = async { ApiClient.apiService.getMyAppointments() }

                val profileRes = profileDeferred.await()
                val aptRes = aptDeferred.await()

                if (profileRes.isSuccessful) {
                    _userProfile.value = profileRes.body()
                } else {
                    _error.value = "Failed to load profile."
                }

                if (aptRes.isSuccessful) {
                    val appointments = aptRes.body() ?: emptyList()
                    calculateStats(appointments)
                }
            } catch (e: Exception) {
                _error.value = "Network error while loading data."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateStats(appointments: List<edu.cit.ochavillo.schedease.network.AppointmentResponse>) {
        var completed = 0
        var upcoming = 0
        val now = LocalDate.now()

        appointments.forEach { apt ->
            val status = apt.status.uppercase()
            if (status == "COMPLETED") {
                completed++
            } else if (status == "CONFIRMED" || status == "PENDING") {
                try {
                    val aptDate = LocalDate.parse(apt.appointmentDate)
                    if (!aptDate.isBefore(now)) upcoming++
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }
        }
        _activityStats.value = ActivityStats(appointments.size, completed, upcoming)
    }

    fun updateProfile(firstName: String, lastName: String, username: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val payload = UpdateProfileRequest(firstName, lastName, username)
                val response = ApiClient.apiService.updateProfile(payload)

                if (response.isSuccessful) {
                    _userProfile.value = response.body()
                    _successMessage.value = "Profile updated successfully!"
                } else {
                    _error.value = "Failed to update profile."
                }
            } catch (e: Exception) {
                _error.value = "Network error while updating."
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resendVerification() {
        val email = _userProfile.value?.email ?: return

        viewModelScope.launch {
            _isResending.value = true
            _resendMessage.value = null

            try {
                val response = ApiClient.apiService.resendVerification(email)
                if (response.isSuccessful) {
                    _resendMessage.value = "Verification email sent! Please check your inbox."
                } else if (response.code() == 429) {
                    _resendMessage.value = "Too many attempts. Please wait 1 minute."
                } else {
                    _resendMessage.value = "Failed to send verification email."
                }
            } catch (e: Exception) {
                _resendMessage.value = "Network error while resending."
            } finally {
                _isResending.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
        _resendMessage.value = null
    }
}