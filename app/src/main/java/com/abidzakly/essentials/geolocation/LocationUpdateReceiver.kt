package com.abidzakly.essentials.geolocation

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

// Simple observer class for location updates
class LocationUpdateObserver {

    companion object {
        // Get the LiveData from the service
        fun getLocationUpdates(): LiveData<LocationUpdate> {
            return LocationTrackingService.locationUpdates
        }
    }
}

// Extension function to easily observe location updates in Activities/Fragments
fun androidx.lifecycle.LifecycleOwner.observeLocationUpdates(
    onLocationUpdate: (LocationUpdate) -> Unit
): Observer<LocationUpdate> {
    val observer = Observer<LocationUpdate> { locationUpdate ->
        onLocationUpdate(locationUpdate)
    }

    LocationUpdateObserver.getLocationUpdates().observe(this, observer)
    return observer
}

// Alternative approach using callback interface
interface LocationUpdateListener {
    fun onLocationUpdate(locationUpdate: LocationUpdate)
}

class LocationUpdateManager {
    private var listener: LocationUpdateListener? = null
    private var observer: Observer<LocationUpdate>? = null

    fun setLocationUpdateListener(
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        listener: LocationUpdateListener
    ) {
        this.listener = listener

        observer = Observer { locationUpdate ->
            listener.onLocationUpdate(locationUpdate)
        }

        LocationTrackingService.locationUpdates.observe(lifecycleOwner, observer!!)
    }

    fun removeLocationUpdateListener() {
        listener = null
        observer = null
    }
}