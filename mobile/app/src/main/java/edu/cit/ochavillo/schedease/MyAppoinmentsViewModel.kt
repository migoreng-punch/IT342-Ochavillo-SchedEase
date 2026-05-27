package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.AppointmentResponse
import edu.cit.ochavillo.schedease.network.UpdateStatusRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyAppointmentsViewModel : ViewModel() {

    private val _appointments = MutableStateFlow<List<AppointmentResponse>>(emptyList())
    val appointments: StateFlow<List<AppointmentResponse>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchMyAppointments()
    }

    fun fetchMyAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = ApiClient.apiService.getMyAppointments()
                if (response.isSuccessful) {
                    _appointments.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load appointments."
                }
            } catch (e: Exception) {
                _error.value = "Network error while fetching appointments."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelAppointment(id: String) {
        viewModelScope.launch {
            try {
                // 1. Fire the API call
                val request = UpdateStatusRequest("CANCELLED")
                val response = ApiClient.apiService.updateAppointmentStatus(id, request)

                if (response.isSuccessful) {
                    // 2. Optimistic UI Update (Matches your React logic!)
                    _appointments.value = _appointments.value.map { apt ->
                        if (apt.id == id) apt.copy(status = "CANCELLED") else apt
                    }
                } else {
                    _error.value = "Failed to cancel appointment."
                }
            } catch (e: Exception) {
                _error.value = "Network error while cancelling."
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}