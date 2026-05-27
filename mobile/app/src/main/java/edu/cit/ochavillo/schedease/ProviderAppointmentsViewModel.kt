package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.ProviderAppointmentResponse
import edu.cit.ochavillo.schedease.network.UpdateStatusRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProviderAppointmentsViewModel : ViewModel() {

    private var allAppointments = listOf<ProviderAppointmentResponse>()

    private val _filteredAppointments = MutableStateFlow<List<ProviderAppointmentResponse>>(emptyList())
    val filteredAppointments: StateFlow<List<ProviderAppointmentResponse>> = _filteredAppointments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentFilter = "ALL"

    init {
        fetchAppointments()
    }

    fun fetchAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = ApiClient.apiService.getProviderAppointments()
                if (response.isSuccessful) {
                    // Sort chronologically
                    val sorted = (response.body() ?: emptyList()).sortedBy { "${it.appointmentDate}T${it.startTime}" }
                    allAppointments = sorted
                    applyFilter()
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

    fun setFilter(filter: String) {
        currentFilter = filter
        applyFilter()
    }

    private fun applyFilter() {
        if (currentFilter == "ALL") {
            _filteredAppointments.value = allAppointments
        } else {
            _filteredAppointments.value = allAppointments.filter {
                it.status.equals(currentFilter, ignoreCase = true)
            }
        }
    }

    fun updateStatus(id: String, newStatus: String) {
        viewModelScope.launch {
            // 1. Optimistic UI Update for a snappy feel
            val previousList = allAppointments
            allAppointments = allAppointments.map { apt ->
                if (apt.id == id) apt.copy(status = newStatus) else apt
            }
            applyFilter()

            // 2. Fire API call
            try {
                val request = UpdateStatusRequest(newStatus)
                val response = ApiClient.apiService.updateAppointmentStatus(id, request)
                if (!response.isSuccessful) {
                    // Revert on failure
                    allAppointments = previousList
                    applyFilter()
                    _error.value = "Failed to update status."
                }
            } catch (e: Exception) {
                // Revert on network crash
                allAppointments = previousList
                applyFilter()
                _error.value = "Network error. Reverting change."
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}