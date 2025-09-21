package com.example.meteodroid.Weather.Models
import Location.LocationData
import com.squareup.moshi.Json

data class WeatherResponse(
    @Json(name = "coord") val location: LocationData?,
    val weather: List<Weather>,
    val base: String?,
    val main: Main?,
    val visibility: Int?,
    val wind: Wind?,
    val rain: Rain?,
    val clouds: Clouds?,
    val dt: Long?,
    val sys: Sys?,
    val timezone: Int?,
    val id: Long?,
    val name: String?,
    val cod: Int?
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    @Json(name = "feels_like") val feelsLike: Double,
    @Json(name = "temp_min") val tempMin: Double,
    @Json(name = "temp_max") val tempMax: Double,
    val pressure: Int,
    val humidity: Int
)

data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double?
)

data class Rain(
    @Json(name = "1h") val lastHour: Double?
)

data class Clouds(
    val all: Int
)

data class Sys(
    val type: Int?,
    val id: Int?,
    val country: String?,
    val sunrise: Long?,
    val sunset: Long?
)

