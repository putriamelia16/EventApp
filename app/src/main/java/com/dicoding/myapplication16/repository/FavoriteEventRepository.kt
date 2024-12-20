package com.dicoding.myapplication16.repository


import com.dicoding.myapplication16.data.database.FavoriteEvent
import com.dicoding.myapplication16.data.database.FavoriteEventDao

import kotlinx.coroutines.flow.Flow

class FavoriteEventRepository(private val favoriteEventDao: FavoriteEventDao) {
    fun getAllFavoriteEvents(): Flow<List<FavoriteEvent>> = favoriteEventDao.getAllFavoriteEvent()

    suspend fun insert(favoriteEvent: FavoriteEvent) = favoriteEventDao.insert(favoriteEvent)

    suspend fun delete(favoriteEvent: FavoriteEvent) = favoriteEventDao.delete(favoriteEvent)

    fun getFavoriteEventById(id: String): Flow<FavoriteEvent?> =
        favoriteEventDao.getFavoriteEventById(id)
}