package edu.cit.ochavillo.schedease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.ochavillo.schedease.network.ApiClient
import edu.cit.ochavillo.schedease.network.Establishment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _establishments = MutableStateFlow<List<Establishment>>(emptyList())
    val establishments: StateFlow<List<Establishment>> = _establishments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var nextCursor: String? = null
    private var currentSearchQuery = ""
    private var searchJob: Job? = null

    init {
        fetchEstablishments(isRefresh = true)
    }

    // Matches the React useEffect debounce pattern
    fun updateSearchQuery(query: String) {
        currentSearchQuery = query
        searchJob?.cancel() // Cancel previous timer

        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            fetchEstablishments(isRefresh = true)
        }
    }

    fun loadMore() {
        if (!_hasMore.value || _isLoadingMore.value) return
        fetchEstablishments(isRefresh = false)
    }

    private fun fetchEstablishments(isRefresh: Boolean) {
        viewModelScope.launch {
            if (isRefresh) {
                _isLoading.value = true
                nextCursor = null
            } else {
                _isLoadingMore.value = true
            }

            try {
                val response = ApiClient.apiService.getEstablishments(
                    search = currentSearchQuery.takeIf { it.isNotBlank() },
                    limit = 6,
                    cursor = nextCursor
                )

                if (response.isSuccessful && response.body() != null) {
                    val responseData = response.body()!!

                    if (isRefresh) {
                        _establishments.value = responseData.data
                    } else {
                        _establishments.value = _establishments.value + responseData.data
                    }

                    nextCursor = responseData.nextCursor
                    _hasMore.value = responseData.hasMore
                }
            } catch (e: Exception) {
                e.printStackTrace() // Add actual error handling if needed
            } finally {
                _isLoading.value = false
                _isLoadingMore.value = false
            }
        }
    }
}