package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.AvailabilityDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Internal state model for the UI
data class ScheduleDay(
    val dayOfWeek: String,
    var isWorkingDay: Boolean,
    var startTime: String,
    var endTime: String
)

class ProviderScheduleViewModel : ViewModel() {

    private val weekTemplate = listOf(
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    )

    private val _schedule = MutableStateFlow<List<ScheduleDay>>(emptyList())
    val schedule: StateFlow<List<ScheduleDay>> = _schedule.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _saveSuccess = MutableStateFlow<Boolean?>(null)
    val saveSuccess: StateFlow<Boolean?> = _saveSuccess.asStateFlow()

    init {
        fetchSchedule()
    }

    fun fetchSchedule() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = ApiClient.apiService.getAvailability()
                val dbData = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()

                val mergedSchedule = weekTemplate.map { dayName ->
                    val savedDay = dbData.find { it.dayOfWeek == dayName }

                    if (savedDay != null) {
                        ScheduleDay(
                            dayOfWeek = savedDay.dayOfWeek,
                            isWorkingDay = !savedDay.startTime.isNullOrBlank() && !savedDay.endTime.isNullOrBlank(),
                            startTime = savedDay.startTime?.take(5) ?: "", // Extract "HH:mm"
                            endTime = savedDay.endTime?.take(5) ?: ""
                        )
                    } else {
                        ScheduleDay(dayName, false, "", "")
                    }
                }
                _schedule.value = mergedSchedule

            } catch (e: Exception) {
                _error.value = "Could not load your schedule."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateToggle(dayOfWeek: String, isWorking: Boolean) {
        _schedule.value = _schedule.value.map { day ->
            if (day.dayOfWeek == dayOfWeek) {
                val updated = day.copy(isWorkingDay = isWorking)
                if (!isWorking) {
                    updated.startTime = ""
                    updated.endTime = ""
                }
                updated
            } else day
        }
    }

    fun updateTime(dayOfWeek: String, isStartTime: Boolean, time: String) {
        _schedule.value = _schedule.value.map { day ->
            if (day.dayOfWeek == dayOfWeek) {
                if (isStartTime) day.copy(startTime = time) else day.copy(endTime = time)
            } else day
        }
    }

    fun saveSchedule() {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            _saveSuccess.value = null

            try {
                // 1. Filter and Format payload for Spring Boot
                val payload = _schedule.value
                    .filter { it.isWorkingDay && it.startTime.isNotBlank() && it.endTime.isNotBlank() }
                    .map { day ->
                        AvailabilityDto(
                            dayOfWeek = day.dayOfWeek,
                            startTime = if (day.startTime.length == 5) "${day.startTime}:00" else day.startTime,
                            endTime = if (day.endTime.length == 5) "${day.endTime}:00" else day.endTime
                        )
                    }

                // 2. Send to backend
                val response = ApiClient.apiService.updateAvailability(payload)
                if (response.isSuccessful) {
                    _saveSuccess.value = true
                    fetchSchedule() // Refresh UI data
                } else {
                    _error.value = "Failed to save changes."
                }
            } catch (e: Exception) {
                _error.value = "Network error while saving."
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _saveSuccess.value = null
    }
}