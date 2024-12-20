package com.dicoding.myapplication16.ui.detail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.myapplication16.data.database.FavoriteEvent
import com.dicoding.myapplication16.data.database.response.Event
import com.dicoding.myapplication16.data.database.retrofit.ApiConfig
import com.dicoding.myapplication16.data.database.retrofit.UiState
import com.dicoding.myapplication16.repository.FavoriteEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.*

class DetailViewModel(private val repository: FavoriteEventRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Event?>>(UiState.Loading)
    val uiState: StateFlow<UiState<Event?>> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun fetchEventDetails(eventId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = ApiConfig.apiService.getDetailEvent(eventId)
                if (result.event != null) {
                    _uiState.value = UiState.Success(result.event, type = 0) // Set an appropriate type
                    checkFavoriteStatus(eventId)
                } else {
                    _uiState.value = UiState.Error("Event details not found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    when (e) {
                        is HttpException -> "Error: ${e.code()}"
                        else -> "Network error: ${e.message}"
                    }
                )
            }
        }
    }

    private fun checkFavoriteStatus(eventId: String) {
        viewModelScope.launch {
            _isFavorite.value = repository.getFavoriteEventById(eventId).firstOrNull() != null
        }
    }

    fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState !is UiState.Success || currentState.data == null) return
        val currentEvent = currentState.data
        val currentFavoriteStatus = _isFavorite.value

        viewModelScope.launch {
            if (currentFavoriteStatus) {
                repository.delete(
                    FavoriteEvent(
                        id = currentEvent.id.toString(),
                        name = currentEvent.name ?: "",
                        mediaCover = currentEvent.mediaCover
                    )
                )
            } else {
                repository.insert(FavoriteEvent(
                    id = currentEvent.id.toString(),
                    name = currentEvent.name ?: "",
                    mediaCover = currentEvent.mediaCover
                ))
            }
            _isFavorite.value = !currentFavoriteStatus
        }
    }
}
