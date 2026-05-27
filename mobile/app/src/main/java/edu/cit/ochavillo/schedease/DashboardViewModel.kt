package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.AppointmentResponse
import edu.cit.ochavillo.schedease.network.RescheduleRequest
import edu.cit.ochavillo.schedease.network.UpdateStatusRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardStats(
    val upcoming: Int = 0,
    val pending: Int = 0,
    val completed: Int = 0,
    val cancelled: Int = 0
)

class DashboardViewModel : ViewModel() {

    private var allAppointments = listOf<AppointmentResponse>()

    private val _filteredAppointments = MutableStateFlow<List<AppointmentResponse>>(emptyList())
    val filteredAppointments: StateFlow<List<AppointmentResponse>> = _filteredAppointments.asStateFlow()

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Reschedule States
    private val _availableTimes = MutableStateFlow<List<String>>(emptyList())
    val availableTimes: StateFlow<List<String>> = _availableTimes.asStateFlow()

    private val _isTimesLoading = MutableStateFlow(false)
    val isTimesLoading: StateFlow<Boolean> = _isTimesLoading.asStateFlow()

    private var currentSearch = ""
    private var currentTab = "All"

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.apiService.getMyAppointments()
                if (response.isSuccessful) {
                    allAppointments = response.body() ?: emptyList()
                    calculateStats()
                    applyFilters()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearch(query: String) {
        currentSearch = query
        applyFilters()
    }

    fun updateTab(tab: String) {
        currentTab = tab
        applyFilters()
    }

    private fun applyFilters() {
        _filteredAppointments.value = allAppointments.filter { apt ->
            val matchesSearch = apt.establishmentName?.contains(currentSearch, ignoreCase = true) == true
            val matchesTab = when (currentTab) {
                "Upcoming" -> apt.status.equals("CONFIRMED", true) || apt.status.equals("PENDING", true)
                "Completed" -> apt.status.equals("COMPLETED", true)
                "Cancelled" -> apt.status.equals("CANCELLED", true)
                else -> true
            }
            matchesSearch && matchesTab
        }
    }

    private fun calculateStats() {
        var upcoming = 0
        var pending = 0
        var completed = 0
        var cancelled = 0

        // In a real app, compare appointment date to LocalDate.now() for 'upcoming'
        allAppointments.forEach { apt ->
            when (apt.status.uppercase()) {
                "CONFIRMED" -> upcoming++
                "PENDING" -> pending++
                "COMPLETED" -> completed++
                "CANCELLED" -> cancelled++
            }
        }
        _stats.value = DashboardStats(upcoming, pending, completed, cancelled)
    }

    fun cancelAppointment(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.updateAppointmentStatus(id, UpdateStatusRequest("CANCELLED"))
                if (response.isSuccessful) {
                    fetchDashboardData() // Refresh data
                    onSuccess()
                } else {
                    onError("Failed to cancel.")
                }
            } catch (e: Exception) {
                onError("Network error.")
            }
        }
    }

    // --- RESCHEDULE LOGIC ---
    fun fetchAvailableTimes(establishmentId: String, date: LocalDate) {
        viewModelScope.launch {
            _isTimesLoading.value = true
            _availableTimes.value = emptyList()
            try {
                val response = ApiClient.apiService.getAvailableSlots(establishmentId, date.toString())
                if (response.isSuccessful) {
                    _availableTimes.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isTimesLoading.value = false
            }
        }
    }

    fun submitReschedule(appointmentId: String, date: LocalDate, time: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = RescheduleRequest(date.toString(), time)
                val response = ApiClient.apiService.rescheduleAppointment(appointmentId, request)
                if (response.isSuccessful) {
                    fetchDashboardData()
                    onSuccess()
                } else {
                    onError("Failed to reschedule.")
                }
            } catch (e: Exception) {
                onError("Network error.")
            }
        }
    }
}