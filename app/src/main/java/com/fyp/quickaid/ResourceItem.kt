package com.fyp.quickaid

data class ResourceItem(
    val name: String,
    val currentStock: Int,
    val currentUnit: String,
    val distributed: Int,
    val distributedUnit: String,
    val totalCapacity: Int,
    val capacityUnit: String,
    val inventoryPercentage: Int,
    val status: ResourceStatus
)

enum class ResourceStatus {
    INCREASING,
    DECREASING,
    STABLE
}