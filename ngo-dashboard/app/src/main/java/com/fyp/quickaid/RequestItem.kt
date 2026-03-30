package com.fyp.quickaid

data class RequestItem(
    val id: String = "",  // ← NO @DocumentId!
    val location: String = "",
    val priority: String = "",
    val requestedBy: String = "",
    val timeAgo: String = "",
    val status: String = "pending",
    val items: List<RequestedItem> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class RequestedItem(
    val name: String = "",
    val quantity: String = ""
)