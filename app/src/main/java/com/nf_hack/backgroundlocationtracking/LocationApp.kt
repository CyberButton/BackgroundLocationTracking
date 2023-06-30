package com.nf_hack.backgroundlocationtracking

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class LocationApp: Application() {

    override fun onCreate() {
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelLow = NotificationChannel(
                "location_low",
                "Location Low",
                NotificationManager.IMPORTANCE_LOW
            )
            val channelDefault = NotificationChannel(
                "location_default",
                "Location Default",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val channelHigh = NotificationChannel(
                "location_high",
                "Location High",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelLow)
            notificationManager.createNotificationChannel(channelDefault)
            notificationManager.createNotificationChannel(channelHigh)
        }
    }
}