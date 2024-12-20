package com.dicoding.myapplication16.di
import android.content.Context
import com.dicoding.myapplication16.data.database.FavoriteEventRoomDatabase
import com.dicoding.myapplication16.repository.FavoriteEventRepository
import com.dicoding.myapplication16.ui.setting.SettingPreferences
import com.dicoding.myapplication16.ui.setting.dataStore


object Injection {
    fun provideRepository(context: Context): FavoriteEventRepository {
        val database by lazy { FavoriteEventRoomDatabase.getDatabase(context) }
        return FavoriteEventRepository(database.favoriteEventDao())
    }

    fun provideSettingPreferences(context: Context): SettingPreferences {
        return SettingPreferences.getInstance(context.dataStore)
    }
}