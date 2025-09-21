package com.example.meteodroid.Weather.Models

data class DailyWeatherUIState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val results: List<DailyWeatherResponseItem> = emptyList(),
    val isEmpty: Boolean = false
)
