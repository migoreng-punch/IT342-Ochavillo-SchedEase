package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.AppointmentRequest
import edu.cit.ochavillo.schedease.network.Establishment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class EstablishmentDetailViewModel : ViewModel() {

    private val _establishment = MutableStateFlow<Establishment?>(null)
    val establishment: StateFlow<Establishment?> = _establishment.asStateFlow()

    private val _availableSlots = MutableStateFlow<List<String>>(emptyList())
    val availableSlots: StateFlow<List<String>> = _availableSlots.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isBooking = MutableStateFlow(false)
    val isBooking: StateFlow<Boolean> = _isBooking.asStateFlow()

    private val _bookingSuccess = MutableStateFlow<Boolean?>(null)
    val bookingSuccess: StateFlow<Boolean?> = _bookingSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var selectedDate: LocalDate = LocalDate.now()
        private set
    var selectedTime: String? = null
        private set

    fun fetchEstablishmentAndSlots(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val estResponse = ApiClient.apiService.getEstablishment(id)
                if (estResponse.isSuccessful) {
                    _establishment.value = estResponse.body()
                    fetchSlots(id, selectedDate)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load establishment."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDateAndFetchSlots(id: String, newDate: LocalDate) {
        selectedDate = newDate
        selectedTime = null // Reset time on new date
        fetchSlots(id, newDate)
    }

    fun selectTime(time: String) {
        selectedTime = time
    }

    private fun fetchSlots(id: String, date: LocalDate) {
        viewModelScope.launch {
            try {
                // Formatting LocalDate to YYYY-MM-DD
                val dateString = date.toString()
                val response = ApiClient.apiService.getAvailableSlots(id, dateString)
                if (response.isSuccessful) {
                    _availableSlots.value = response.body() ?: emptyList()
                } else {
                    _availableSlots.value = emptyList()
                }
            } catch (e: Exception) {
                _availableSlots.value = emptyList()
            }
        }
    }

    fun bookAppointment(establishmentId: String) {
        if (selectedTime == null) return

        viewModelScope.launch {
            _isBooking.value = true
            _errorMessage.value = null
            try {
                val request = AppointmentRequest(
                    establishmentId = establishmentId,
                    date = selectedDate.toString(),
                    startTime = selectedTime!!
                )
                val response = ApiClient.apiService.bookAppointment(request)
                if (response.isSuccessful) {
                    _bookingSuccess.value = true
                } else {
                    _errorMessage.value = "Failed to book appointment: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error while booking."
            } finally {
                _isBooking.value = false
            }
        }
    }

    fun resetMessages() {
        _errorMessage.value = null
        _bookingSuccess.value = null
    }
}