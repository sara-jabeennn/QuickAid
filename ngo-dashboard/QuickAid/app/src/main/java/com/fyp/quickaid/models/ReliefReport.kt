package com.fyp.quickaid.models

data class ReliefReport(
    val id: String = "",
    val ngoId: String = "",
    val ngoName: String = "",
    val date: String = "",
    val area: String = "",
    val peopleHelped: Int = 0,
    val reliefType: String = "", // "Food", "Medical", "Shelter", "Clothing", "Water"
    val description: String = "",
    val mediaUrls: List<String> = emptyList(), // Images/documents
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Submitted" // "Submitted", "Reviewed", "Approved"
)