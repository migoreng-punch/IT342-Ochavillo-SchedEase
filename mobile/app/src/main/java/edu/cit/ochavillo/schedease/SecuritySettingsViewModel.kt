package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.PasswordChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecuritySettingsViewModel : ViewModel() {

    private val _isChangingPassword = MutableStateFlow(false)
    val isChangingPassword: StateFlow<Boolean> = _isChangingPassword.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    // Used for showing Toast/SnackBar feedback on the UI
    private val _feedbackMessage = MutableStateFlow<Pair<String, Boolean>?>(null) // Pair(Message, isSuccess)
    val feedbackMessage: StateFlow<Pair<String, Boolean>?> = _feedbackMessage.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    fun changePassword(current: String, newPass: String, confirm: String) {
        if (current.isBlank() || newPass.isBlank() || confirm.isBlank()) {
            _feedbackMessage.value = Pair("All fields are required.", false)
            return
        }

        if (newPass != confirm) {
            _feedbackMessage.value = Pair("New passwords do not match.", false)
            return
        }

        viewModelScope.launch {
            _isChangingPassword.value = true
            try {
                val request = PasswordChangeRequest(current, newPass, confirm)
                val response = ApiClient.apiService.changePassword(request)

                if (response.isSuccessful) {
                    _feedbackMessage.value = Pair("Password updated successfully!", true)
                } else {
                    _feedbackMessage.value = Pair("Failed to update password. Please check your current password.", false)
                }
            } catch (e: Exception) {
                _feedbackMessage.value = Pair("Network error while changing password.", false)
            } finally {
                _isChangingPassword.value = false
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                val response = ApiClient.apiService.deleteAccount() // Assuming this exists from previous step
                if (response.isSuccessful) {
                    _deleteSuccess.value = true
                } else {
                    _feedbackMessage.value = Pair("Failed to delete account.", false)
                }
            } catch (e: Exception) {
                _feedbackMessage.value = Pair("Network error while deleting account.", false)
            } finally {
                _isDeleting.value = false
            }
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }
}