package com.fyp.quickaid.models

data class Resource(
    val id: String = "",
    val name: String = "", // "Food Packets", "Medicine", "Blankets"
    val quantity: Int = 0,
    val region: String = "",
    val campaign: String = "",
    val deliveryStatus: String = "Pending", // "Pending", "In Progress", "Delivered"
    val allocatedBy: String = "",
    val allocatedDate: String = "",
    val deliveredDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
)