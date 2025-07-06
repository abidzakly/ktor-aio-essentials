package com.abidzakly.essentials.geolocation

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class LocationTrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "LocationTrackingChannel"
        const val NOTIFICATION_ID = 1

        // Location update intervals
        const val UPDATE_INTERVAL = 10000L // 10 seconds
        const val FASTEST_UPDATE_INTERVAL = 5000L // 5 seconds
        const val MINIMUM_DISPLACEMENT = 5f // 5 meters

        // LiveData for location updates
        private val _locationUpdates = MutableLiveData<LocationUpdate>()
        val locationUpdates: androidx.lifecycle.LiveData<LocationUpdate> = _locationUpdates

        // Method to post location updates
        internal fun postLocationUpdate(locationUpdate: LocationUpdate) {
            _locationUpdates.postValue(locationUpdate)
        }
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var nominatimHelper: NominatimHelperClient
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var isTracking = false
    private var lastKnownLocation: Location? = null

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        nominatimHelper = NominatimHelperClient(this)

        createLocationRequest()
        createLocationCallback()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_TRACKING" -> startLocationTracking()
            "STOP_TRACKING" -> stopLocationTracking()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL)
            .setMinUpdateDistanceMeters(MINIMUM_DISPLACEMENT)
            .setMaxUpdateDelayMillis(UPDATE_INTERVAL * 2)
            .build()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                if (!locationAvailability.isLocationAvailable) {
                    updateNotification("Location not available")
                }
            }
        }
    }

    private fun handleLocationUpdate(location: Location) {
        lastKnownLocation = location

        // Update notification with current coordinates
        updateNotification("Lat: ${location.latitude}, Lng: ${location.longitude}")

        // Get address in background
        serviceScope.launch {
            try {
                val addressResult = nominatimHelper.getAddress(location.latitude, location.longitude)
                addressResult.onSuccess { address ->
                    // Post location update with address
                    postLocationUpdate(LocationUpdate(location, address))

                    // Update notification with address
                    updateNotification("📍 ${address.name}")
                }
                addressResult.onFailure {
                    // Post location update without address
                    postLocationUpdate(LocationUpdate(location, null))
                }
            } catch (e: Exception) {
                // Post location update without address
                postLocationUpdate(LocationUpdate(location, null))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationTracking() {
        if (!hasLocationPermissions()) {
            stopSelf()
            return
        }

        if (isTracking) return

        isTracking = true

        val notification = createNotification("Starting location tracking...")
        startForeground(NOTIFICATION_ID, notification)

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun stopLocationTracking() {
        if (!isTracking) return

        isTracking = false
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for location tracking service"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = "STOP_TRACKING"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking Active")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        nominatimHelper.close()
        if (isTracking) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }
}

// Helper class to manage the service
class LocationTrackingManager(private val context: Context) {

    fun startTracking() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = "START_TRACKING"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopTracking() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = "STOP_TRACKING"
        }
        context.startService(intent)
    }
}

// Data class for location updates
data class LocationUpdate(
    val location: Location,
    val address: AddressResult? = null,
    val timestamp: Long = System.currentTimeMillis()
)