package com.fyp.quickaid_communication.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long,
    val messageType: Int,
    val hopCount: Int,
    val pathJson: String,
    val isSent: Boolean,
    val isReceived: Boolean
)