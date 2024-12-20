package com.dicoding.myapplication16.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.myapplication16.data.database.response.ListEventsItem
import com.dicoding.myapplication16.data.database.retrofit.ApiService
import com.dicoding.myapplication16.data.database.retrofit.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class EventViewModel(private val apiService: ApiService) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<ListEventsItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ListEventsItem>>> = _uiState.asStateFlow()

    private val _networkState = MutableStateFlow(true)
    val networkState: StateFlow<Boolean> = _networkState.asStateFlow()

    fun setNetworkState(isConnected: Boolean) {
        _networkState.value = isConnected
    }

    fun fetchEvents(eventStatus: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = apiService.getEvents(eventStatus)

                // Check if the response.listEvents is null or empty
                val events: List<ListEventsItem?>? = response.listEvents

                if (events.isNullOrEmpty()) {
                    _uiState.value = UiState.Error("No events found")
                } else {
                    // Filter out null values to create a non-nullable list
                    val nonNullEvents: List<ListEventsItem> = events.filterNotNull()

                    // Check if we still have events after filtering
                    if (nonNullEvents.isEmpty()) {
                        _uiState.value = UiState.Error("No valid events found")
                    } else {
                        // Pass eventStatus as type
                        _uiState.value = UiState.Success(nonNullEvents, eventStatus)
                    }
                }
            } catch (e: HttpException) {
                _uiState.value = UiState.Error("Error: ${e.code()} - ${e.message()}")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Network error: ${e.message}")
            }
        }
    }
}
