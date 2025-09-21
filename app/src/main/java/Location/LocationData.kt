package Location

import com.squareup.moshi.Json

/**
 * A model model for representing latitude and longitude.
 */
data class LocationData(
    @Json(name = "lat") val latitude: Double,
    @Json(name = "lon") val longtitude: Double
)