package com.fyp.quickaid.models

data class Volunteer(
    val id: String,
    val name: String,
    val location: String,
    val skills: List<String>,
    val rating: Float,
    val tasksCompleted: Int,
    val isBusy: Boolean
)