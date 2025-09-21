package com.example.meteodroid.Weather

import com.example.meteodroid.Weather.Models.DailyWeatherResponse
import com.example.meteodroid.Weather.Models.WeatherResponse
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPIService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("lang") lang: String = "ru"
        ): WeatherResponse

    @GET("data/2.5/forecast/daily")
    suspend fun getWeatherForWeeks(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("cnt") count: Int = 14
    ): DailyWeatherResponse
}