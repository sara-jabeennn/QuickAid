package com.fyp.quickaid

data class HistoryItem(
    val id: String = "",  // ← NO @DocumentId!
    val location: String = "",
    val recipient: String = "",
    val items: List<DistributedItem> = emptyList(),
    val timeAgo: String = "",
    val status: String = "completed",
    val timestamp: Long = System.currentTimeMillis()
)

data class DistributedItem(
    val name: String = "",
    val quantity: String = ""
)