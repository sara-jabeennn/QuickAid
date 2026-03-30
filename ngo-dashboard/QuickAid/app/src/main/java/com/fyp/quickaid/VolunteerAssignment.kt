package com.fyp.quickaid.models

data class VolunteerAssignment(
    val id: String = "",
    val volunteerId: String = "",
    val volunteerName: String = "",
    val region: String = "",
    val assignedBy: String = "",
    val assignedDate: Long = System.currentTimeMillis(),
    val status: String = "Active", // Active or Completed
    val victimId: String? = null,
    val victimName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)