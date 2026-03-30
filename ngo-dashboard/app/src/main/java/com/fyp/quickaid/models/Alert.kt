package com.fyp.quickaid.models

data class Alert(
    val id: String,
    val title: String,
    val location: String,
    val time: String,
    val description: String,
    val affected: String,
    val priority: String // "critical", "high", "medium", "low"
)