package com.nf_hack.backgroundlocationtracking

import okhttp3.*
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

// service for background execution
class LocationService: Service() {

    // bound to the lifetime of the service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // location client abstraction
    private lateinit var locationClient: LocationClient

    // dont bind to anything
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    // when service is first created
    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    // called for every single intent sent for the service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // if intent has a function
        when(intent?.action) {
            ACTION_START -> {
                val latitude = intent.getStringExtra("LATITUDE")
                val longitude = intent.getStringExtra("LONGITUDE")
                start(latitude, longitude)
            }
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(customLatitude: String? = null, customLongitude: String? = null) {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")   // initially null
            .setSmallIcon(R.drawable.bee_aware1024x1021)    //change to beeAWARE
            .setOngoing(true)   //cant swipe away

        // reference to a notification manager, to update notification text
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // save previous danger state
        var previousDangerLevel = "0"

        locationClient
            .getLocationUpdates(5000L) // every 5 seconds
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
//                val lat = location.latitude.toString()
//                val long = location.longitude.toString()

                // for testing with custom input
                val lat = customLatitude ?: location.latitude.toString()
                val long = customLongitude ?: location.longitude.toString()

                Log.d(
                    "CALL",
                    lat + long
                )

                val dangerLevel = apiCall(latitude = lat, longitude = long)

                if (dangerLevel != previousDangerLevel) {
                    previousDangerLevel = dangerLevel

                val channel = when (dangerLevel) {
                    "1" -> "location_low"
                    "2" -> "location_default"
                    "3" -> "location_high"
                    else -> "location_default"
                }

                val msg = when (dangerLevel) {
                    "1" -> "safe"
                    "2" -> "Medium"
                    "3" -> "HIGH!"
                    else -> "safe"
                }

                val localNotification = NotificationCompat.Builder(this, channel)
                    .setContentTitle("Tracking location...")
                    .setContentText("Danger level: $msg")
                    .setSmallIcon(R.drawable.bee_aware1024x1021)
                    .setOngoing(true)

                notificationManager.notify(1, localNotification.build())
                }
            }
            .launchIn(serviceScope)

        // to make this a foreground service
        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private suspend fun apiCall(latitude: String, longitude: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getData(latitude, longitude)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Log.d("CALL", responseBody.toString())
                        return@withContext responseBody.danger_level.toString()
                    } else {
                        Log.d("CALL", "response body is null")
                        return@withContext "response body is null"
                    }
                } else {
                    Log.d("CALL", "error")
                    return@withContext "error"
                }
            } catch (t: Throwable) {
                Log.d("CALL", t.toString())
                return@withContext t.toString()
            }
        }
    }


    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}