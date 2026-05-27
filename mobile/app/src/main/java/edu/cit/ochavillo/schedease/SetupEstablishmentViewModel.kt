package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.CreateEstablishmentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SetupEstablishmentViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    fun createEstablishment(
        name: String,
        email: String,
        durationStr: String,
        address: String,
        description: String
    ) {
        if (name.isBlank() || email.isBlank() || address.isBlank()) {
            _error.value = "Please fill in all required fields."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val payload = CreateEstablishmentRequest(
                    name = name.trim(),
                    description = description.trim(),
                    address = address.trim(),
                    contactEmail = email.trim(),
                    slotDurationMinutes = durationStr.toIntOrNull() ?: 30
                )

                val response = ApiClient.apiService.createEstablishment(payload)

                if (response.isSuccessful) {
                    _success.value = true
                } else {
                    _error.value = "Failed to create establishment. Please check your inputs."
                }
            } catch (e: Exception) {
                _error.value = "Network error. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}