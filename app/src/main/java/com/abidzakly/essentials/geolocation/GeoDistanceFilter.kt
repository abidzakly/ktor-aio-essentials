
package com.abidzakly.essentials.geolocation

import kotlinx.serialization.Serializable
import kotlin.math.*

// Data classes
@Serializable
data class Coordinate(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90 degrees" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180 degrees" }
    }
}

@Serializable
data class Employee(
    val id: String,
    val name: String,
    val coordinate: Coordinate,
    val distance: Double? = null,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class FilterResult(
    val filteredEmployees: List<Employee>,
    val totalFound: Int,
    val maxDistance: Double,
    val userCoordinate: Coordinate,
    val processingTimeMs: Long
)

// Main GeoDistanceFilter class
class GeoDistanceFilter {

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
        private const val DEFAULT_MAX_DISTANCE_METERS = 1000.0

        // Factory method for easy instantiation
        fun create(): GeoDistanceFilter = GeoDistanceFilter()
    }

    /**
     * Filter employees within specified distance from user location
     * @param userCoordinate User's current location
     * @param employees List of employees with their coordinates
     * @param maxDistanceMeters Maximum distance in meters (default: 1000m)
     * @param sortByDistance Whether to sort results by distance (default: true)
     * @return FilterResult containing filtered employees and metadata
     */
    fun filterEmployees(
        userCoordinate: Coordinate,
        employees: List<Employee>,
        maxDistanceMeters: Double = DEFAULT_MAX_DISTANCE_METERS,
        sortByDistance: Boolean = true
    ): FilterResult {
        val startTime = System.currentTimeMillis()

        require(maxDistanceMeters > 0) { "Max distance must be positive" }
        require(employees.isNotEmpty()) { "Employee list cannot be empty" }

        val filteredEmployees = employees
            .asSequence()
            .map { employee ->
                val distance = calculateDistance(userCoordinate, employee.coordinate)
                employee.copy(distance = distance)
            }
            .filter { it.distance!! <= maxDistanceMeters }
            .let { sequence ->
                if (sortByDistance) sequence.sortedBy { it.distance }.toList()
                else sequence.toList()
            }

        val processingTime = System.currentTimeMillis() - startTime

        return FilterResult(
            filteredEmployees = filteredEmployees,
            totalFound = filteredEmployees.size,
            maxDistance = maxDistanceMeters,
            userCoordinate = userCoordinate,
            processingTimeMs = processingTime
        )
    }

    /**
     * Filter employees within multiple distance ranges
     * @param userCoordinate User's current location
     * @param employees List of employees
     * @param distanceRanges List of distance ranges in meters
     * @return Map of distance ranges to filtered results
     */
    fun filterByMultipleRanges(
        userCoordinate: Coordinate,
        employees: List<Employee>,
        distanceRanges: List<Double>
    ): Map<Double, FilterResult> {
        return distanceRanges.associateWith { range ->
            filterEmployees(userCoordinate, employees, range)
        }
    }

    /**
     * Get closest N employees regardless of distance
     * @param userCoordinate User's current location
     * @param employees List of employees
     * @param count Number of closest employees to return
     * @return FilterResult with closest employees
     */
    fun getClosestEmployees(
        userCoordinate: Coordinate,
        employees: List<Employee>,
        count: Int
    ): FilterResult {
        val startTime = System.currentTimeMillis()

        require(count > 0) { "Count must be positive" }
        require(employees.isNotEmpty()) { "Employee list cannot be empty" }

        val closestEmployees = employees
            .asSequence()
            .map { employee ->
                val distance = calculateDistance(userCoordinate, employee.coordinate)
                employee.copy(distance = distance)
            }
            .sortedBy { it.distance }
            .take(count)
            .toList()

        val maxDistance = closestEmployees.lastOrNull()?.distance ?: 0.0
        val processingTime = System.currentTimeMillis() - startTime

        return FilterResult(
            filteredEmployees = closestEmployees,
            totalFound = closestEmployees.size,
            maxDistance = maxDistance,
            userCoordinate = userCoordinate,
            processingTimeMs = processingTime
        )
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * @param coord1 First coordinate
     * @param coord2 Second coordinate
     * @return Distance in meters
     */
    fun calculateDistance(coord1: Coordinate, coord2: Coordinate): Double {
        val lat1Rad = Math.toRadians(coord1.latitude)
        val lat2Rad = Math.toRadians(coord2.latitude)
        val deltaLatRad = Math.toRadians(coord2.latitude - coord1.latitude)
        val deltaLonRad = Math.toRadians(coord2.longitude - coord1.longitude)

        val a = sin(deltaLatRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLonRad / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c * 1000 // Convert to meters
    }

    /**
     * Check if a coordinate is within a specific area (bounding box)
     * @param coordinate Coordinate to check
     * @param center Center of the area
     * @param radiusMeters Radius in meters
     * @return True if within area
     */
    fun isWithinArea(
        coordinate: Coordinate,
        center: Coordinate,
        radiusMeters: Double
    ): Boolean {
        return calculateDistance(coordinate, center) <= radiusMeters
    }

    /**
     * Get statistics about distances
     * @param userCoordinate User's location
     * @param employees List of employees
     * @return DistanceStatistics
     */
    fun getDistanceStatistics(
        userCoordinate: Coordinate,
        employees: List<Employee>
    ): DistanceStatistics {
        require(employees.isNotEmpty()) { "Employee list cannot be empty" }

        val distances = employees.map {
            calculateDistance(userCoordinate, it.coordinate)
        }

        return DistanceStatistics(
            minDistance = distances.minOrNull() ?: 0.0,
            maxDistance = distances.maxOrNull() ?: 0.0,
            averageDistance = distances.average(),
            totalEmployees = employees.size
        )
    }
}

// Distance statistics data class
@Serializable
data class DistanceStatistics(
    val minDistance: Double,
    val maxDistance: Double,
    val averageDistance: Double,
    val totalEmployees: Int
)

// Extension functions for easier usage
fun List<Employee>.filterByDistance(
    userCoordinate: Coordinate,
    maxDistanceMeters: Double = 1000.0
): FilterResult {
    return GeoDistanceFilter().filterEmployees(userCoordinate, this, maxDistanceMeters)
}

fun List<Employee>.getClosest(
    userCoordinate: Coordinate,
    count: Int
): FilterResult {
    return GeoDistanceFilter().getClosestEmployees(userCoordinate, this, count)
}
