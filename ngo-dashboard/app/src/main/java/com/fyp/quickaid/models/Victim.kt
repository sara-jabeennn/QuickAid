package com.fyp.quickaid.models

data class Victim(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val needs: List<String>,
    val priority: VictimPriority,
    val status: VictimStatus,
    val peopleCount: Int,
    val updatedMinutesAgo: Int
)

enum class VictimPriority {
    CRITICAL, HIGH, MEDIUM, LOW
}

enum class VictimStatus {
    ACTIVE, ASSISTED
}