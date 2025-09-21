package com.example.meteodroid.Cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.meteodroid.Weather.Models.DailyWeatherUIState

@Composable
fun WeatherForecastCard(state: DailyWeatherUIState) {
    when {
        state.isLoading -> {
            CircularProgressIndicator()
        }
        state.isEmpty -> {
            Text("No data")
        }
        state.errorMessage != null -> {
            Text(text = state.errorMessage)
        }
        !state.results.isEmpty() -> {
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.results.count()) { index ->
                    val item = state.results[index]
                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val iconName = item.weather.firstOrNull()?.icon ?: ""
                            AsyncImage(
                                model = "https://openweathermap.org/img/wn/$iconName@2x.png",
                                contentDescription = "Weather Icon",
                                modifier = Modifier.size(42.dp)
                            )
                            Text(text = "${item.temperature.value.toString()}°")
                            Text(text = item.weather.firstOrNull()?.condition ?: "-")
                        }
                    }

                }
            }
        }
    }
}