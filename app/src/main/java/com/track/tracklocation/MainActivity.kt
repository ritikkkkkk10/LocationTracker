package com.track.tracklocation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002
        const val BACKGROUND_NOTIFICATION_PERMISSION_REQUEST_CODE = 1003
        const val FOREGROUND_NOTIFICATION_PERMISSION_REQUEST_CODE = 1004
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAndRequestPermissions()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun checkAndRequestPermissions() {
        // Check if the app has foreground location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // For Android 10 (API 29) and above, check if background location permission is granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Log.d("Permission", "Both foreground and background location permissions are granted")

                // For Android 12 (API 31) and above, check if FOREGROUND_SERVICE_LOCATION permission is granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Log.d("Permission", "Requesting FOREGROUND_SERVICE_LOCATION permission")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.FOREGROUND_SERVICE_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // For Android 13 (API 33) and above, check and request notification permission
                    checkNotificationPermission()
                } else {
                    // If all permissions are granted, start location updates
                    startLocationUpdates()
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Request background location permission if not granted
                Log.d("Permission", "Requesting background location permission")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // Start location updates if only foreground location permission is required
                Log.d("Permission", "Only foreground location permission is needed")
                startLocationUpdates()
            }
        } else {
            // Request both foreground and background location permissions (if needed)
            val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)

            // Add background location permission if the device is running Android 10 or above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }

            // Add FOREGROUND_SERVICE_LOCATION permission if the device is running Android 12 or above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
            }

            Log.d("Permission", "Requesting required location permissions")
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If foreground permission is granted, now check for background location permission
                    checkAndRequestPermissions()
                } else {
                    // Permission denied for foreground location
                    Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show()
                }
            }
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If background location permission is granted, check for notification permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        checkNotificationPermission()
                    } else {
                        startLocationUpdates()
                    }
                } else {
                    // Permission denied for background location
                    Toast.makeText(this, "Background location permission is required.", Toast.LENGTH_SHORT).show()
                }
            }
            BACKGROUND_NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "Notification permission granted.")
                    startLocationUpdates()
                } else {
                    Log.d("Permission", "Notification permission denied.")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU) // For Android 13 and above
    fun checkNotificationPermission() {
        Log.d("Notification", "POST_NOTIFICATIONS permission: ${
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Notification permission granted.")
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                BACKGROUND_NOTIFICATION_PERMISSION_REQUEST_CODE // Request code for notification permission
            )
        }
    }

    fun startLocationUpdates() {
        Log.d("Location", "Starting location updates")
        // Start the location service to track location
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show()
    }
}
