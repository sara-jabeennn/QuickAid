package com.fyp.quickaid.models

data class Volunteer(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val skills: List<String> = emptyList(),
    val rating: Float = 0f,
    val tasksCompleted: Int = 0,
    val isBusy: Boolean = false,

    // NEW FIELDS for NGO Module
    val isAvailable: Boolean = true,
    val assignedRegion: String = "",
    val assignedDate: String = "",
    val assignedBy: String = "", // NGO ID who assigned
    val phone: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
