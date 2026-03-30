package com.fyp.quickaid.models

data class VictimRequest(
    val id: String = "",
    val name: String = "",
    val priority: String = "",
    val category: String = "",
    val description: String = "",
    val location: String = "",
    val timeAgo: String = "",
    val phoneNumber: String = "",
    val teamCount: Int = 0,
    val status: String = "pending",
    val region: String = "",
    val requestType: String = "",
    val severity: String = ""
)