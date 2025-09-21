package com.example.meteodroid.CitySearch

data class CitySearchUIState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val results: List<CitySearchResponse> = emptyList(),
    val isEmpty: Boolean = false
)
