package com.example.meteodroid.Cards

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.meteodroid.R
import com.example.meteodroid.Weather.WeatherViewModel
enum class WeatherDetailsCardType(
    val title: String,
    @DrawableRes val iconRes: Int
) {
    Wind("Wind", R.drawable.windsock),
    Humidity("Humidity", R.drawable.hygrometer),
    Pressure("Pressure",R.drawable.barometer)
}

@Composable
fun WeatherDetailsCard(viewModel: WeatherViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), // общий отступ для всего ряда
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WeatherDetailsCardType.entries.forEach {
            DetailsCard(
                type = it,
                value = when (it) {
                    WeatherDetailsCardType.Wind -> "${viewModel.weatherResponse?.wind?.speed ?: 0}m/s"
                    WeatherDetailsCardType.Humidity -> "${viewModel.weatherResponse?.main?.humidity ?: 0}%"
                    WeatherDetailsCardType.Pressure -> "${viewModel.weatherResponse?.main?.pressure ?: 0} hPa"
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DetailsCard(type: WeatherDetailsCardType, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = type.iconRes),
                contentDescription = null
            )
            Text(type.title, fontWeight = FontWeight.Bold)
            Text(value)
        }
    }
}