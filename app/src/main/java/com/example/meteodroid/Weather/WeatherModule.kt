package com.example.meteodroid.Weather

import Database.CityCashDAO
import android.content.Context
import com.example.meteodroid.CitySearch.CitySearchAPIService
import com.example.meteodroid.CitySearch.CitySearchRepository
import com.example.meteodroid.Settings.SettingsRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {
    @Provides
    fun providerRetrofit(): Retrofit {
        // Base URL of the OpenWeatherMap API
        val baseUrl = "https://api.openweathermap.org/"
        // Moshi instance used for JSON serialization/deserialization
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        // HTTP logging interceptor for debugging network calls
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        // OkHttp client with the logging interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // Initialized instance of the WeatherApiService using Retrofit
    @Provides
    fun provideWeatherAPIService(retrofit: Retrofit): WeatherAPIService {
        return retrofit.create(WeatherAPIService::class.java)
    }

    // Initialized instance of the weatherRepository
    @Provides
    fun provideWeatherRepository(apiService: WeatherAPIService, cityCashDAO: CityCashDAO): WeatherRepository {
        return WeatherRepository(apiService, cityCashDAO)
    }

    @Provides
    fun provideCitySearchRepository(apiService: CitySearchAPIService): CitySearchRepository {
        return CitySearchRepository(apiService)
    }

    @Provides
    fun provideCitySearchAPIService(retrofit: Retrofit): CitySearchAPIService {
        return retrofit.create(CitySearchAPIService::class.java)
    }

    @Provides
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository = SettingsRepository(context)
}