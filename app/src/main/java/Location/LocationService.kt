package Location
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface LocationService {
    suspend fun getLastKnownLocation(): LocationFetchResult
    val isLocationPermissionGranted: Boolean
}

/**
 * LocationService provides safe and reusable access to device location
 * using FusedLocationProviderClient (Google Play Services).
 *
 * It supports both one-time and continuous location requests,
 * while respecting runtime permission checks and accuracy fallbacks.
 */
class DefaultLocationService @Inject constructor (@ApplicationContext private val context: Context): LocationService {
    // This is the central component of the location structure. Once created, you use it to request location updates and retrieve the last known location
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    // A callback used to receive notifications when the device's location has changed or
    // can no longer be determined
    private var locationCallback: LocationCallback? = null

    /**
    * Returns true if fine location permission (GPS) is granted.
    */
    val isFineLocationGranted: Boolean
        get() = ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    /**
     * Returns true if fine coarse permission (approximate) is granted.
     */
    val isCoarseLocationGranted: Boolean
        get() = ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    /**
     * Returns true if any location permission is granted.
     */
    override val isLocationPermissionGranted: Boolean
        get() = isFineLocationGranted || isCoarseLocationGranted

    /**
     * Returns the highest available location accuracy based on granted permissions,
     * or null if location access is denied.
     */
    fun getBestAccuracyAvailable(): Int? {
        return when {
            isFineLocationGranted -> Priority.PRIORITY_HIGH_ACCURACY
            isCoarseLocationGranted -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            else -> null
        }
    }

    /**
     * Starts continuous location updates and invokes the callback
     * on every new location fix. Accuracy level is based on granted permission.
     */
    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    fun startLocationUpdates(onLocation: (LocationData) -> UInt) {
        val accuracy = getBestAccuracyAvailable() ?: return
        val request = LocationRequest.Builder(accuracy, 5000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    onLocation(it.toLocationData())
                }
            }
        }

        fusedClient.requestLocationUpdates(
            request,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    /**
     * Attempts to get the last known location (cached by Android).
     * Returns null if permission is missing or location is unavailable.
     */
    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])

   override suspend fun getLastKnownLocation(): LocationFetchResult = suspendCoroutine { cont ->
        val accuracy = getBestAccuracyAvailable()
        if (accuracy == null) {
            cont.resume(LocationFetchResult.PermissionDenied)
            return@suspendCoroutine
        }
        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(LocationFetchResult.Success(location.toLocationData()))
                } else {
                    cont.resume(LocationFetchResult.LocationUnavailable)
                }
            }
            .addOnFailureListener { cont.resume(LocationFetchResult.Error(it)) }
    }

    /**
     * Converts Android's Location object to our own lightweight LocationData.
     */
    private fun Location.toLocationData(): LocationData {
        return LocationData(latitude = latitude, longtitude = longitude)
    }

    /**
     * Stops any previously started location updates to prevent memory leaks or battery drain.
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }
}
