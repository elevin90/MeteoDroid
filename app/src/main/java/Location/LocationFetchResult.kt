package Location

sealed class LocationFetchResult {
    data class Success(val location: LocationData): LocationFetchResult()
    object PermissionDenied: LocationFetchResult()
    object LocationUnavailable: LocationFetchResult()
    data class Error(val throwable: Throwable): LocationFetchResult()
}