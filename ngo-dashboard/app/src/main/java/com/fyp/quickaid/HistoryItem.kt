package com.fyp.quickaid

data class HistoryItem(
    val location: String,
    val recipient: String,
    val items: List<DistributedItem>,
    val timeAgo: String,
    val status: String
)

data class DistributedItem(
    val name: String,
    val quantity: String
)