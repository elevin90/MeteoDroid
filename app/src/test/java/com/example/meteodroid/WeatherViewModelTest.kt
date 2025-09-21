/*
 *
 *
 *
 *  * Copyright (c) 2025 $user. All rights reserved.
 *
 *
 */

// test/util/MainDispatcherRule.kt
import Database.CityCacheEntity
import Location.LocationData
import Location.LocationService
import com.example.meteodroid.Settings.SettingsRepository
import com.example.meteodroid.Settings.TemperatureUnit
import com.example.meteodroid.Weather.Models.DailyWeatherResponse
import com.example.meteodroid.Weather.Models.WeatherResponse
import com.example.meteodroid.Weather.WeatherRepository
import com.example.meteodroid.Weather.WeatherViewModel
import io.mockk.every
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestRule {
    override fun apply(base: Statement, description: Description?) = object : Statement() {
        override fun evaluate() {
            Dispatchers.setMain(dispatcher)
            try { base.evaluate() } finally { Dispatchers.resetMain() }
        }
    }
}


class WeatherViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val locationService = mockk<LocationService>(relaxed = true)
    private val weatherRepository = mockk<WeatherRepository>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)

    private val unitFlow = MutableStateFlow(TemperatureUnit.CELSIUS)
    private val favouritesFlow = MutableStateFlow<List<CityCacheEntity>>(emptyList())
    init {
        // unitFlow из настроек
        every { settingsRepository.unitFlow } returns unitFlow
        // избранные города (для isFavourite/favouriteIds)
        every { weatherRepository.favouritesCitiesFlow } returns favouritesFlow
        // разрешение на локацию по умолчанию — есть
        every { locationService.isLocationPermissionGranted } returns true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `updateForSelectedLocation loads current and daily, updates UI state`() = runTest {
        val current = mockk<WeatherResponse>(relaxed = true).also {
            every { it.id } returns 123L
        }
        val daily = mockk<DailyWeatherResponse>(relaxed = true).also {
            // daily.weatherData -> не пустой список
            every { it.weatherData } returns listOf(mockk())
        }
        coEvery {
            weatherRepository.fetchCurrentWeather(
                location = any(), apiKey = any(), units = any()
            )
        } returns current
        coEvery {
            weatherRepository.fetchDailyWeather(
                location = any(), apiKey = any(), units = any()
            )
        } returns daily

        val vm = WeatherViewModel(locationService, weatherRepository, settingsRepository)

        // Act: подаём локацию
        vm.updateForSelectedLocation(LocationData(latitude = 50.0614, longtitude = 19.9383))
        advanceUntilIdle() // дождаться всех корутин VM

        // Assert: состояние заполнено

        assertEquals(123L, vm.currentCityId.value)
        assertFalse(vm.dailyWeatherState.isLoading)
        assertTrue(vm.dailyWeatherState.errorMessage == null || vm.dailyWeatherState.errorMessage!!.isEmpty())
        assertFalse(vm.dailyWeatherState.isEmpty)
        assertTrue(vm.dailyWeatherState.results.isNotEmpty())
    }
}