package com.fyp.quickaid.models

data class Message(
    val id: String,
    val text: String,
    val senderId: String,
    val senderName: String,
    val timestamp: String,
    val isFromUser: Boolean  // true = sent by user (right side), false = from team (left side)
)