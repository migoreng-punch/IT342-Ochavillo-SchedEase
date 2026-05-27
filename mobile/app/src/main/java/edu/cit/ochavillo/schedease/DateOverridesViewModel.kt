package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.AddOverrideRequest
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.OverrideDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DateOverridesViewModel : ViewModel() {

    private val _overrides = MutableStateFlow<List<OverrideDto>>(emptyList())
    val overrides: StateFlow<List<OverrideDto>> = _overrides.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _addSuccess = MutableStateFlow(false)
    val addSuccess: StateFlow<Boolean> = _addSuccess.asStateFlow()

    init {
        fetchOverrides()
    }

    fun fetchOverrides() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = ApiClient.apiService.getOverrides()
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    // Sort chronologically
                    _overrides.value = data.sortedBy { it.date }
                } else {
                    _error.value = "Failed to load date overrides."
                }
            } catch (e: Exception) {
                _error.value = "Network error while fetching overrides."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addOverride(date: String, isUnavailable: Boolean, startTime: String?, endTime: String?) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            _addSuccess.value = false

            try {
                // Ensure times are formatted properly for Java (append ":00" if needed)
                val formattedStart = if (!isUnavailable && startTime != null) {
                    if (startTime.length == 5) "$startTime:00" else startTime
                } else null

                val formattedEnd = if (!isUnavailable && endTime != null) {
                    if (endTime.length == 5) "$endTime:00" else endTime
                } else null

                val request = AddOverrideRequest(date, isUnavailable, formattedStart, formattedEnd)
                val response = ApiClient.apiService.addOverride(request)

                if (response.isSuccessful) {
                    _addSuccess.value = true
                    fetchOverrides() // Refresh list
                } else {
                    _error.value = "Failed to create override."
                }
            } catch (e: Exception) {
                _error.value = "Network error while adding override."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun deleteOverride(id: String) {
        viewModelScope.launch {
            val previousList = _overrides.value
            // Optimistic UI Update
            _overrides.value = previousList.filter { it.id != id }

            try {
                val response = ApiClient.apiService.deleteOverride(id)
                if (!response.isSuccessful) {
                    _overrides.value = previousList // Revert on fail
                    _error.value = "Failed to delete override."
                }
            } catch (e: Exception) {
                _overrides.value = previousList // Revert on fail
                _error.value = "Network error while deleting."
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _addSuccess.value = false
    }
}