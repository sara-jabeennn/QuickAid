package com.fyp.quickaid.models

import com.google.firebase.firestore.PropertyName

enum class Priority {
    HIGH, MEDIUM, LOW;

    companion object {
        fun fromString(value: String): Priority {
            return when (value.uppercase()) {
                "HIGH" -> HIGH
                "MEDIUM" -> MEDIUM
                "LOW" -> LOW
                else -> MEDIUM
            }
        }
    }
}

data class Region(
    val id: String = "",
    val name: String = "",
    val currentVolunteers: Int = 0,
    val requiredVolunteers: Int = 0,
    @get:PropertyName("priority")
    @set:PropertyName("priority")
    var priorityString: String = "MEDIUM", // Firebase stores as String
    val isActive: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
) {
    // Computed property for Priority enum (not stored in Firebase)
    @get:com.google.firebase.firestore.Exclude
    val priority: Priority
        get() = Priority.fromString(priorityString)

    val percentage: Int
        get() = if (requiredVolunteers > 0) {
            (currentVolunteers * 100) / requiredVolunteers
        } else 0
}