package com.nf_hack.backgroundlocationtracking

import android.location.Location
import kotlinx.coroutines.flow.Flow

// implementation for abstraction, needs to be implemented
interface LocationClient {
    // emits location data
    fun getLocationUpdates(interval: Long): Flow<Location>
    // if somethings goes wrong (ex: no location permission, gps disabled, ...)
    class LocationException(message: String): Exception()
}