package com.example.meteodroid.Weather

import Database.CityCacheEntity
import Database.toCityCacheEntity
import Location.LocationData
import Location.LocationFetchResult
import Location.LocationService
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meteodroid.Settings.SettingsRepository
import com.example.meteodroid.Settings.TemperatureUnit
import com.example.meteodroid.Settings.symbol
import com.example.meteodroid.Weather.Models.DailyWeatherUIState
import com.example.meteodroid.Weather.Models.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val locationService: LocationService,
    private val weatherRepository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    // ---- UI state ----
    var locationResult by mutableStateOf<LocationFetchResult?>(null)
        private set
    var weatherResponse by mutableStateOf<WeatherResponse?>(null)
        private set
    var dailyWeatherState by mutableStateOf<DailyWeatherUIState>(DailyWeatherUIState())
        private set

    val savedCities = weatherRepository.favouritesCitiesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val locationFlow = MutableStateFlow<LocationData?>(null)

    private val _currentCityId = MutableStateFlow<Long?>(null)
    val currentCityId: StateFlow<Long?> = _currentCityId

    private val favouriteIds: StateFlow<Set<Long>> = savedCities
        .map { list -> list.mapNotNull { it.cityId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val isFavourite: StateFlow<Boolean> = combine(favouriteIds, currentCityId) { ids, id ->
        id != null && id in ids
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val unitsFlow: StateFlow<TemperatureUnit> =
        settingsRepository.unitFlow
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, TemperatureUnit.CELSIUS)

    init {
        viewModelScope.launch {
            combine(
                locationFlow.filterNotNull().distinctUntilChanged(),
                unitsFlow
            ) { location, units -> location to units }
                .mapLatest { (location, units) ->
                    // 1. Show loader before fetching weather
                    dailyWeatherState = dailyWeatherState.copy(
                        isLoading = true,
                        errorMessage = null,
                        results = emptyList(),
                        isEmpty = false
                    )
                    val units = units.label

                    // 2. Parallel fetch current and daily forecast
                    coroutineScope {
                        val current = async {
                            weatherRepository.fetchCurrentWeather(
                                location = location,
                                apiKey = "",
                                units = units
                            )
                        }
                        val daily = async {
                            weatherRepository.fetchDailyWeather(
                                location = location,
                                apiKey = "",
                                units = units
                            )
                        }
                        current.await() to daily.await()
                    }
                }
                // 3. Update UI state with results
                .onEach { (current, daily) ->
                    weatherResponse = current
                    dailyWeatherState = dailyWeatherState.copy(
                        isLoading = false,
                        errorMessage = null,
                        results = daily.weatherData,
                        isEmpty = daily.weatherData.isEmpty()
                    )
                    _currentCityId.value = current.id

                    //locationResult = LocationFetchResult.Success(current.loca)
                }
                // 4. Handle errors
                .catch { e ->
                    dailyWeatherState = dailyWeatherState.copy(
                        isLoading = false,
                        errorMessage = e.message.orEmpty(),
                        results = emptyList(),
                        isEmpty = true
                    )
                    _currentCityId.value = null

                }.collect()
        }
    }

    fun selectCity(city: CityCacheEntity) {
        _currentCityId.value = city.cityId
        locationFlow.value = LocationData(latitude = city.lat ?: 0.0, longtitude = city.lon ?: 0.0)
    }

    fun updateForSelectedLocation(location: LocationData) {
        _currentCityId.value = null
        locationFlow.value = location
    }

    fun fetchLocation() {
        if (locationService.isLocationPermissionGranted) {
            viewModelScope.launch {
                locationResult = locationService.getLastKnownLocation()
                if (locationResult is LocationFetchResult.Success) {
                    locationFlow.value = (locationResult as LocationFetchResult.Success).location
                }
            }
        } else {
            locationResult = LocationFetchResult.PermissionDenied
        }
    }

    fun toggleFavourite() = viewModelScope.launch {
        val id = currentCityId.value ?: return@launch
        val entity = weatherResponse?.toCityCacheEntity() ?: return@launch

        if (isFavourite.value) {
            weatherRepository.removeCityWith(id)
        } else {
            weatherRepository.save(entity)
        }
    }
}