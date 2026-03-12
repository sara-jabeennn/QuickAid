package com.fyp.quickaid.models

data class VictimRequest(
    val id: String,
    val name: String,
    val priority: String, // "critical", "high", "medium", "low"
    val category: String, // "Rescue", "Medical", "Food", etc.
    val description: String,
    val location: String,
    val timeAgo: String,
    val phoneNumber: String,
    val teamCount: Int,
    val status: String // "pending", "inProgress", "completed"
)