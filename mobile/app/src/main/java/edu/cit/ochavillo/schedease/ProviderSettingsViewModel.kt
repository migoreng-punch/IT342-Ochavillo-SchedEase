package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.SettingsEstablishmentDto
import edu.cit.ochavillo.schedease.network.SettingsUserDto
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsFormData(
    val establishmentName: String = "",
    val description: String = "",
    val address: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val slotDurationMinutes: Int = 0,
    val bufferMinutes: Int = 0,
    val bookingCutoffHours: Int = 0,
    val username: String = "",
    val firstName: String = "",
    val lastName: String = ""
)

class ProviderSettingsViewModel : ViewModel() {

    private val _formData = MutableStateFlow<SettingsFormData?>(null)
    val formData: StateFlow<SettingsFormData?> = _formData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    init {
        fetchSettings()
    }

    fun fetchSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fire both requests simultaneously (like Promise.all)
                val estabDeferred = async { ApiClient.apiService.getMyEstablishment() }
                val userDeferred = async { ApiClient.apiService.getMyUser() }

                val estabResponse = estabDeferred.await()
                val userResponse = userDeferred.await()

                if (estabResponse.isSuccessful && userResponse.isSuccessful) {
                    val estabData = estabResponse.body()
                    val userData = userResponse.body()

                    if (estabData != null && userData != null) {
                        _formData.value = SettingsFormData(
                            establishmentName = estabData.name,
                            description = estabData.description,
                            address = estabData.address,
                            contactEmail = estabData.contactEmail,
                            contactPhone = estabData.phone,
                            slotDurationMinutes = estabData.slotDurationMinutes,
                            bufferMinutes = estabData.bufferMinutes,
                            bookingCutoffHours = estabData.bookingCutoffHours,
                            username = userData.username,
                            firstName = userData.firstName,
                            lastName = userData.lastName
                        )
                    }
                } else {
                    _error.value = "Could not load settings data."
                }
            } catch (e: Exception) {
                _error.value = "Network error while fetching settings."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSettings(data: SettingsFormData) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            _saveSuccess.value = false

            try {
                val estabPayload = SettingsEstablishmentDto(
                    name = data.establishmentName,
                    description = data.description,
                    address = data.address,
                    contactEmail = data.contactEmail,
                    phone = data.contactPhone,
                    slotDurationMinutes = data.slotDurationMinutes,
                    bufferMinutes = data.bufferMinutes,
                    bookingCutoffHours = data.bookingCutoffHours
                )

                val userPayload = SettingsUserDto(
                    username = data.username,
                    firstName = data.firstName,
                    lastName = data.lastName
                )

                // Fire both PUT requests simultaneously
                val estabUpdate = async { ApiClient.apiService.updateMyEstablishment(estabPayload) }
                val userUpdate = async { ApiClient.apiService.updateMyUser(userPayload) }

                val estabRes = estabUpdate.await()
                val userRes = userUpdate.await()

                if (estabRes.isSuccessful && userRes.isSuccessful) {
                    _saveSuccess.value = true
                } else {
                    _error.value = "Failed to save changes. Please try again."
                }
            } catch (e: Exception) {
                _error.value = "Network error while saving settings."
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteEstablishment() {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null

            try {
                val response = ApiClient.apiService.deleteMyEstablishment()
                if (response.isSuccessful) {
                    _deleteSuccess.value = true
                } else {
                    _error.value = "Failed to delete establishment."
                }
            } catch (e: Exception) {
                _error.value = "Network error while deleting."
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetState() {
        _error.value = null
        _saveSuccess.value = false
    }
}