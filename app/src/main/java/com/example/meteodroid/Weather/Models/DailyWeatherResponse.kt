package com.example.meteodroid.Weather.Models

import com.squareup.moshi.Json
import java.util.concurrent.locks.Condition

data class DailyWeatherResponse(
    @Json(name = "list")  val weatherData: List<DailyWeatherResponseItem>
)

data class DailyWeatherResponseItem(
    @Json(name = "feels_like") val temperature: DailyWeatherResponseDetails,
    val weather: List<DailyWeatherResponseWeather>
)

data class DailyWeatherResponseDetails(
    @Json(name = "day") val value: Double
)

data class DailyWeatherResponseWeather(
    @Json(name = "main") val condition: String,
    val icon: String
)