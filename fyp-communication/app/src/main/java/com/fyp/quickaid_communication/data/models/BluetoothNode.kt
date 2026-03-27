package com.fyp.quickaid_communication.data.models

data class BluetoothNode(
    val id: String,
    val name: String,
    val hasInternet: Boolean = false,
    val role: String = "RELAY",
    val neighbors: MutableList<String> = mutableListOf(),
    var distance: Int = Int.MAX_VALUE,
    var visited: Boolean = false
)