package Database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.meteodroid.Weather.Models.Main
import com.example.meteodroid.Weather.Models.WeatherResponse

// Cached table from API response
@Entity(
    tableName = "city_cache",
    indices = [Index(value = ["dt"]), Index(value = ["name"])]
)
data class CityCacheEntity(
    @PrimaryKey val cityId: Long,   // из WeatherResponse.id
    val name: String?,              // WeatherResponse.name
    val lat: Double?,               // coord.lat
    val lon: Double?,               // coord.lon
    val dt: Long?                   // WeatherResponse.dt (unix seconds)
)
fun WeatherResponse.toCityCacheEntity(): CityCacheEntity? {
    val cityId = id ?: return null
    return CityCacheEntity(
        cityId = cityId,
        name   = name,
        lat    = location?.latitude,
        lon    = location?.longtitude,
        dt     = dt               // unix seconds (UTC)
    )
}