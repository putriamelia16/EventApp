package com.dicoding.myapplication16.data.database.retrofit

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()

    data class Success<T>(val data: T, val type: Int) : UiState<T>() // Add 'type' property

    data class Error(val message: String) : UiState<Nothing>()
}
