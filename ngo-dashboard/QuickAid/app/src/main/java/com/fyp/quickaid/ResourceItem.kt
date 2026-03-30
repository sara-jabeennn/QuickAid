package com.fyp.quickaid

data class ResourceItem(
    val id: String = "",  // ← NO @DocumentId annotation!
    val name: String = "",
    val currentStock: Int = 0,
    val currentUnit: String = "",
    val distributed: Int = 0,
    val distributedUnit: String = "",
    val totalCapacity: Int = 0,
    val capacityUnit: String = "",
    val inventoryPercentage: Int = 0,
    val status: String = "",
    val region: String = "",
    val timestamp: Long = System.currentTimeMillis()
)