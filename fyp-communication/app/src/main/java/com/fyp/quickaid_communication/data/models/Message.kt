package com.fyp.quickaid_communication.data.models

import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: Int,
    var hopCount: Int = 0,
    val path: MutableList<String> = mutableListOf(),
    val ttl: Int = 10
) {
    fun incrementHop(nodeId: String): Message {
        return this.copy(
            hopCount = hopCount + 1,
            path = path.apply { add(nodeId) }
        )
    }

    fun isExpired(): Boolean = hopCount >= ttl
}