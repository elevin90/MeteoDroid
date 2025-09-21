package Location

import android.location.Location

class StubLocationService: LocationService {
    override val isLocationPermissionGranted: Boolean
        get() = true

    override suspend fun getLastKnownLocation(): LocationFetchResult {
       return LocationFetchResult.Success(LocationData(latitude = 0.0, longtitude = 0.0))
    }
}