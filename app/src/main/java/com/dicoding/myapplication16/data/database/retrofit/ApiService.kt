package com.dicoding.myapplication16.data.database.retrofit


import com.dicoding.myapplication16.data.database.response.DetailEventResponse
import com.dicoding.myapplication16.data.database.response.EventResponse

import retrofit2.http.*

interface ApiService {
    @GET("events")
    suspend fun getEvents(@Query("active") active: Int): EventResponse

    @GET("events/{id}")
    suspend fun getDetailEvent(@Path("id") id: String): DetailEventResponse
}