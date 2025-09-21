package com.example.meteodroid.CitySearch

import retrofit2.http.GET
import retrofit2.http.Query

interface CitySearchAPIService {
    @GET("geo/1.0/direct")
    suspend fun getCityPredictions(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("appid") apiKey: String
    ): List<CitySearchResponse>
}

