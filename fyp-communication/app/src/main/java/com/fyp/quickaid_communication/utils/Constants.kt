package com.fyp.quickaid_communication.utils

import java.util.UUID

object Constants {
    // Bluetooth UUID for app identification
    val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    const val APP_NAME = "QuickAid"

    // Message Types
    const val MESSAGE_TYPE_TEXT = 0
    const val MESSAGE_TYPE_ID_EXCHANGE = 99
    const val MESSAGE_TYPE_EMERGENCY = 1
    const val MESSAGE_TYPE_TOPOLOGY_UPDATE = 2
    const val MESSAGE_TYPE_ACK = 3

    // Node Roles
    const val ROLE_SENDER = "SENDER"
    const val ROLE_RECEIVER = "RECEIVER"
    const val ROLE_RELAY = "RELAY"

    // Network
    const val MAX_HOPS = 10 // Maximum hops before message is dropped
    const val DISCOVERY_DURATION = 12000L // 12 seconds
}