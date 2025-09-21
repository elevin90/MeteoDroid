package com.example.meteodroid.CitySearch

class CitySearchRepository(
    private val apiService: CitySearchAPIService
) {
    suspend fun searchCity(query: String, apiKey: String): List<CitySearchResponse> {
        return apiService.getCityPredictions(query = query, apiKey = apiKey)
    }
}