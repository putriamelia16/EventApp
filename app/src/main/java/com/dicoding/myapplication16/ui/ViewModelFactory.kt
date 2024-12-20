package com.dicoding.myapplication16.ui


import android.content.Context
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dicoding.myapplication16.data.database.retrofit.ApiConfig
import com.dicoding.myapplication16.di.Injection
import com.dicoding.myapplication16.ui.detail.DetailViewModel
import com.dicoding.myapplication16.ui.favorite.FavoriteEventViewModel
import com.dicoding.myapplication16.ui.setting.SettingViewModel

object ViewModelFactory {
    fun getInstance(context: Context) = viewModelFactory {
        val repository = Injection.provideRepository(context)
        val pref = Injection.provideSettingPreferences(context)
        val apiService = ApiConfig.apiService

        initializer {
            DetailViewModel(repository)
        }

        initializer {
            FavoriteEventViewModel(repository)
        }

        initializer {
            SettingViewModel(pref)
        }

        initializer {
            EventViewModel(apiService)
        }
    }
}