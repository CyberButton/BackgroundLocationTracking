package com.nf_hack.backgroundlocationtracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch


// implementation of the location client interface
class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
): LocationClient {


    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        // something went wrong
        return callbackFlow {
            if(!(ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)) {
                throw LocationClient.LocationException("Missing location permission")
            }

            // to check for GPS and Internet
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if(!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) && !(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
                throw LocationClient.LocationException("GPS is disabled")
            }

            // create location request(specify - calling interval)
            val request = LocationRequest.create()
                .setInterval(interval)
                .setFastestInterval(interval)

            val locationCallback = object : LocationCallback() {
                // called whenever the new location is fetched
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    // if location exists
                    result.locations.lastOrNull()?.let { location ->
                        launch { send(location) }
                    }
                }
            }

            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            // when we stop collecting location updates
            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}