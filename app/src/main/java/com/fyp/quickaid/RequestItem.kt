package com.fyp.quickaid

data class RequestItem(
    val location: String,
    val priority: String,
    val requestedBy: String,
    val items: List<RequestedResource>,
    val timeAgo: String
)

data class RequestedResource(
    val name: String,
    val quantity: String
)