package com.fyp.quickaid.models

enum class Priority {
    HIGH, MEDIUM, LOW
}

data class Region(
    val id: String,
    val name: String,
    val currentVolunteers: Int,
    val requiredVolunteers: Int,
    val priority: Priority
) {
    val percentage: Int
        get() = (currentVolunteers * 100) / requiredVolunteers
}