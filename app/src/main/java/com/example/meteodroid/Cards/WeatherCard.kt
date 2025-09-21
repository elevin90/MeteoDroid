package com.example.meteodroid.Cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.meteodroid.Weather.Models.WeatherResponse

@Composable
fun WeatherCard(
    weather: WeatherResponse?,
    isFavourite: Boolean,
    onEdit: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.LightGray, RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        val icon = weather?.weather?.firstOrNull()?.icon ?: ""
        val url = "https://openweathermap.org/img/wn/${icon}@2x.png"
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = weather?.name.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = {
                    onSave()
                }) {
                    val icon = if (isFavourite) Icons.Filled.Star else Icons.Outlined.Star
                    val tint = if (isFavourite) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant

                    Icon(
                        imageVector = icon,
                        contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                        tint = tint
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                // Edit button
                IconButton(onClick = {
                    onEdit()
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit city"
                    )
                }
            }
            Text(
                text = weather?.weather?.firstOrNull()?.description?.capitalize() ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Horizontal
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                )
                {
                    Text(
                        text = "${weather?.main?.temp?.toInt() ?: 0}°",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = "Feels like ${weather?.main?.feelsLike?.toInt() ?: 0}°",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                AsyncImage(
                    model = url,
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun WeatherCardPreview() {
//    WeatherCard(
//        city = "Berlin",
//        weatherDescription = "Sunny",
//        temperature = 25,
//        feelsLike = 20,
//        iconName = "sun",
//        onEdit = { }
//    )
//}