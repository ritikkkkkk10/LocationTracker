package com.track.tracklocation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        Log.d("ABABABABAB", "Inside Location Service Client")

        // Create the notification channel for devices running Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_service_channel",
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Start the foreground service with a notification
        val notification = buildNotification("Waiting for location...")
        startForeground(1, notification)  // Start the service in the foreground
    }

    private fun buildNotification(locationText: String): android.app.Notification {
        Log.d("Notification", "Creating notification with channel ID:")
        return NotificationCompat.Builder(this, "location_service_channel")
            .setContentTitle("Tracking Location")
            .setContentText(locationText)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }


    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // High accuracy priority
            10000 // Interval in milliseconds
        ).build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    lastLatitude = location.latitude
                    lastLongitude = location.longitude

                    Log.d("LocationService", "Lat: ${location.latitude}, Lng: ${location.longitude}")

                    // Update the notification with the new location
                    val locationText = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                    val notification = buildNotification(locationText)

                    // Update the notification
                    val manager = getSystemService(NotificationManager::class.java)
                    manager.notify(1, notification)
                }
            }
        }, Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "onStartCommand called")
        startLocationUpdates()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
