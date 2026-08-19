package com.example.speedsnapshot

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color // <-- Added this import for your color function!
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import android.os.Looper

const val LOCATION_REQUEST_CODE = 100

class MainActivity : AppCompatActivity() {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        2000L   // ask for a new location every 2 seconds
    ).build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return

            val speedMs = location.speed
            val speedKmh = speedMs * 3.6f
            val accuracy = location.accuracy

            tvSpeed.text = "%.1f km/h".format(speedKmh)
            tvAccuracy.text = "Accuracy: %.1f m".format(accuracy)
            updateSpeedColor(speedKmh)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkAndRequestPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted! Tap Start.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to track speed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Integrated UI function
    fun updateSpeedColor(speedKmh: Float) {
        val color = when {
            speedKmh < 10f -> Color.GREEN
            speedKmh < 30f -> Color.parseColor("#FFC107") // amber
            else           -> Color.RED
        }
        tvSpeed.setTextColor(color)
    }
}