package com.example.speedsnapshot

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

const val LOCATION_REQUEST_CODE = 100

class MainActivity : AppCompatActivity() {

    // Views - bound in onCreate()
    lateinit var tvSpeed: TextView
    lateinit var tvAccuracy: TextView
    lateinit var btnStart: Button
    lateinit var btnStop: Button

    // Location client - needed to actually request/remove updates
    lateinit var fusedClient: FusedLocationProviderClient

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

        // Bind views to the layout
        tvSpeed = findViewById(R.id.tvSpeed)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        btnStart.setOnClickListener {
            if (hasLocationPermission()) {
                try {
                    fusedClient.requestLocationUpdates(
                        locationRequest, locationCallback, Looper.getMainLooper()
                    )
                } catch (e: SecurityException) {
                    Toast.makeText(this, "Permission was denied.", Toast.LENGTH_SHORT).show()
                }
            } else {
                checkAndRequestPermission()
            }
        }

        btnStop.setOnClickListener {
            fusedClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (e: SecurityException) {
            // no-op, nothing to clean up if permission was never granted
        }
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

    fun updateSpeedColor(speedKmh: Float) {
        val color = when {
            speedKmh < 10f -> Color.GREEN
            speedKmh < 30f -> Color.parseColor("#FFC107") // amber
            else           -> Color.RED
        }
        tvSpeed.setTextColor(color)
    }
}