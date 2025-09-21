package com.example.meteodroid.Weather

import Database.CityCacheEntity
import Database.CityCashDAO
import Location.LocationData
import com.example.meteodroid.Weather.Models.DailyWeatherResponse
import com.example.meteodroid.Weather.Models.WeatherResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor (
    private val apiService: WeatherAPIService,
    private val cityCashDAO: CityCashDAO
) {

    val favouritesCitiesFlow: Flow<List<CityCacheEntity>> =
        cityCashDAO.getAllSortedByFreshness()

    suspend fun fetchCurrentWeather(
        location: LocationData,
        apiKey: String,
        units: String
    ): WeatherResponse {
        return apiService.getCurrentWeather(
            latitude = location.latitude,
            longitude = location.longtitude,
            apiKey = apiKey,
            units = units
        )
    }

    suspend fun fetchDailyWeather(
        location: LocationData,
        apiKey: String,
        units: String
    ): DailyWeatherResponse {
        return apiService.getWeatherForWeeks(
            latitude = location.latitude,
            longitude = location.longtitude,
            apiKey = apiKey,
            units = units
        )
    }

    suspend fun save(cityCacheEntity: CityCacheEntity?) {
        cityCacheEntity?.let {
            cityCashDAO.upsertCity(city = cityCacheEntity)
        }
    }

    suspend fun removeCityWith(id: Long?) {
        id?.let {
            cityCashDAO.removeCity(cityId = id)
        }
    }
}