package com.dicoding.myapplication16.ui.favorite


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.myapplication16.data.database.FavoriteEvent
import com.dicoding.myapplication16.data.database.retrofit.UiState
import com.dicoding.myapplication16.repository.FavoriteEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
class FavoriteEventViewModel(private val repository: FavoriteEventRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<FavoriteEvent>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<FavoriteEvent>>> = _uiState.asStateFlow()

    init {
        fetchFavorites()
    }

    private fun fetchFavorites() {
        viewModelScope.launch {
            repository.getAllFavoriteEvents()
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { favorites ->
                    _uiState.value = UiState.Success(favorites, type = 1) // Pass the appropriate type here
                }
        }
    }
}
