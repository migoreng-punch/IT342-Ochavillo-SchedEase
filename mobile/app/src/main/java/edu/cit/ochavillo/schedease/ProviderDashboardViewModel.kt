package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.ProviderAppointmentResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProviderStats(
    val total: Int = 0,
    val pending: Int = 0,
    val confirmed: Int = 0,
    val clients: Int = 0
)

class ProviderDashboardViewModel : ViewModel() {

    private val _stats = MutableStateFlow(ProviderStats())
    val stats: StateFlow<ProviderStats> = _stats.asStateFlow()

    private val _recentActivity = MutableStateFlow<List<ProviderAppointmentResponse>>(emptyList())
    val recentActivity: StateFlow<List<ProviderAppointmentResponse>> = _recentActivity.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchProviderData()
    }

    fun fetchProviderData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = ApiClient.apiService.getProviderAppointments()
                if (response.isSuccessful) {
                    val appointments = response.body() ?: emptyList()
                    processDashboardData(appointments)
                } else {
                    _error.value = "Failed to load dashboard data."
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun processDashboardData(appointments: List<ProviderAppointmentResponse>) {
        var pendingCount = 0
        var confirmedCount = 0
        val uniqueClients = mutableSetOf<String>()

        appointments.forEach { apt ->
            val status = apt.status.uppercase()
            if (status == "PENDING") pendingCount++
            if (status == "CONFIRMED") confirmedCount++

            apt.clientName?.let { name ->
                if (name.isNotBlank()) uniqueClients.add(name)
            }
        }

        _stats.value = ProviderStats(
            total = appointments.size,
            pending = pendingCount,
            confirmed = confirmedCount,
            clients = uniqueClients.size
        )

        // Sort chronologically (earliest first, matching your React logic)
        val sortedApts = appointments.sortedBy { "${it.appointmentDate}T${it.startTime}" }

        // Take top 5 for Recent Activity
        _recentActivity.value = sortedApts.take(5)
    }
}