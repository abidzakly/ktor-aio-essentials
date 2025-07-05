package com.abidzakly.essentials.geolocation

// Usage Examples and Tests
class GeoDistanceFilterExample {
    
    fun exampleUsage() {
        // Create sample data
        val userLocation = Coordinate(-6.2088, 106.8456) // Jakarta
        val employees = listOf(
            Employee("1", "John", Coordinate(-6.2090, 106.8460)),
            Employee("2", "Jane", Coordinate(-6.2100, 106.8470)),
            Employee("3", "Bob", Coordinate(-6.3000, 106.9000)) // Far away
        )
        
        // Create filter instance
        val filter = GeoDistanceFilter.create()
        
        // Example 1: Filter within 1000 meters
        val result = filter.filterEmployees(
            userCoordinate = userLocation,
            employees = employees,
            maxDistanceMeters = 1000.0
        )
        
        println("Found ${result.totalFound} employees within 1000m")
        result.filteredEmployees.forEach { emp ->
            println("${emp.name}: ${emp.distance?.toInt()}m away")
        }
        
        // Example 2: Get 2 closest employees
        val closest = filter.getClosestEmployees(userLocation, employees, 2)
        
        // Example 3: Using extension functions
        val quickFilter = employees.filterByDistance(userLocation, 500.0)
        val quickClosest = employees.getClosest(userLocation, 1)
        
        // Example 4: Get statistics
        val stats = filter.getDistanceStatistics(userLocation, employees)
        println("Average distance: ${stats.averageDistance.toInt()}m")
    }
}