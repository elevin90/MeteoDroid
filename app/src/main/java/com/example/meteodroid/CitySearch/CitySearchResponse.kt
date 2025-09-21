package com.example.meteodroid.CitySearch

import com.squareup.moshi.Json

data class CitySearchResponse(
    val name: String,
    @Json(name = "lat") val latitude: Double,
    @Json(name = "lon") val longtitude: Double,
    val country: String
)
