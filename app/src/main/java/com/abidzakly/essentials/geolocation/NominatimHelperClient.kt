package com.abidzakly.essentials.geolocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NominatimHelperClient(private val context: Context) {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
                encodeDefaults = false
            })
        }
        install(Logging) {
            level = LogLevel.NONE
        }
    }

    /**
     * Get address information from current device location
     */
    suspend fun getCurrentLocationAddress(): Result<AddressResult> {
        return try {
            val location = getCurrentLocation()
            getAddress(location.first, location.second)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get address information from provided coordinates
     */
    suspend fun getAddress(
        latitude: Double,
        longitude: Double
    ): Result<AddressResult> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("https://nominatim.openstreetmap.org/reverse") {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("format", "json")
            }

            val nominatimResponse = response.body<NominatimResponse>()

            // Extract name from display_name (text before first comma)
            val name = nominatimResponse.displayName.split(",").firstOrNull()?.trim() ?: ""

            val addressResult = AddressResult(
                name = name,
                displayName = nominatimResponse.displayName,
                latitude = nominatimResponse.lat.toDoubleOrNull() ?: latitude,
                longitude = nominatimResponse.lon.toDoubleOrNull() ?: longitude
            )

            Result.success(addressResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current device coordinates
     */
    suspend fun getCurrentLocation(highAccuracy: Boolean = true): Pair<Double, Double> {
        if (!areLocationPermissionsGranted()) {
            throw SecurityException("Location permissions not granted")
        }

        // Try to get last known location first
        try {
            val lastLocation = getLastLocation()
            if (lastLocation != null) {
                return lastLocation
            }
        } catch (e: Exception) {
            // If last location fails, continue to current location
        }

        // Get current location if last location is null or failed
        return getCurrentLocationInternal(highAccuracy)
    }

    /**
     * Retrieves the last known user location
     */
    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(): Pair<Double, Double>? =
        suspendCancellableCoroutine { continuation ->
            if (!areLocationPermissionsGranted()) {
                continuation.resumeWithException(SecurityException("Location permissions not granted"))
                return@suspendCancellableCoroutine
            }

            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    /**
     * Retrieves the current user location
     */
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocationInternal(highAccuracy: Boolean): Pair<Double, Double> =
        suspendCancellableCoroutine { continuation ->
            if (!areLocationPermissionsGranted()) {
                continuation.resumeWithException(SecurityException("Location permissions not granted"))
                return@suspendCancellableCoroutine
            }

            val priority = if (highAccuracy) Priority.PRIORITY_HIGH_ACCURACY
            else Priority.PRIORITY_BALANCED_POWER_ACCURACY

            val cancellationTokenSource = CancellationTokenSource()

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }

            fusedLocationProviderClient.getCurrentLocation(priority, cancellationTokenSource.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        continuation.resumeWithException(Exception("Unable to get current location"))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    /**
     * Checks if location permissions are granted
     */
    fun areLocationPermissionsGranted(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * Search function by query
     */
    suspend fun searchLocationByName(query: String): AddressResult? =
        withContext(Dispatchers.IO) {
            try {
                val client = HttpClient(Android) {
                    install(ContentNegotiation) {
                        json(Json {
                            ignoreUnknownKeys = true
                            prettyPrint = false
                            encodeDefaults = false
                        })
                    }
                }

                val response = client.get("https://nominatim.openstreetmap.org/search") {
                    parameter("q", query)
                    parameter("format", "json")
                    parameter("limit", "1")
                }

                val searchResults = response.body<List<NominatimSearchResult>>()
                client.close()

                if (searchResults.isNotEmpty()) {
                    val result = searchResults.first()
                    AddressResult(
                        name = result.displayName.split(",").firstOrNull()?.trim() ?: "",
                        displayName = result.displayName,
                        latitude = result.lat.toDoubleOrNull() ?: 0.0,
                        longitude = result.lon.toDoubleOrNull() ?: 0.0
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Search function by query
     */
    /**
     * Search function by query, returning a list of results (limit: 20)
     */
    suspend fun searchLocationsByName(query: String): List<AddressResult> =
        withContext(Dispatchers.IO) {
            try {
                val client = HttpClient(Android) {
                    install(ContentNegotiation) {
                        json(Json {
                            ignoreUnknownKeys = true
                            prettyPrint = false
                            encodeDefaults = false
                        })
                    }
                }

                val response = client.get("https://nominatim.openstreetmap.org/search") {
                    parameter("q", query)
                    parameter("format", "json")
                    parameter("limit", "20")
                }

                val searchResults = response.body<List<NominatimSearchResult>>()
                client.close()

                searchResults.mapNotNull { result ->
                    val lat = result.lat.toDoubleOrNull()
                    val lon = result.lon.toDoubleOrNull()

                    if (lat != null && lon != null) {
                        AddressResult(
                            name = result.displayName.split(",").firstOrNull()?.trim() ?: "",
                            displayName = result.displayName,
                            latitude = lat,
                            longitude = lon
                        )
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }


    fun close() {
        client.close()
    }
}

@Serializable
internal data class NominatimResponse(
    @SerialName("display_name")
    val displayName: String,
    val lat: String,
    val lon: String
)

@Serializable
data class AddressResult(
    var name: String = "",
    var displayName: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)

@Serializable
private data class NominatimSearchResult(
    @SerialName("display_name")
    val displayName: String,
    val lat: String,
    val lon: String
)