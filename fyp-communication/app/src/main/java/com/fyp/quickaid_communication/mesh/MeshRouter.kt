package com.fyp.quickaid_communication.mesh

import android.util.Log
import com.google.gson.Gson
import com.fyp.quickaid_communication.data.database.AppDatabase
import com.fyp.quickaid_communication.data.database.MessageEntity
import com.fyp.quickaid_communication.data.models.BluetoothNode
import com.fyp.quickaid_communication.data.models.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MeshRouter(
    private val database: AppDatabase,
    private val myNodeId: String
) {
    private val pathFinder = PathFinder()
    private val nodes = mutableMapOf<String, BluetoothNode>()
    private val gson = Gson()
    private val processedMessages = mutableSetOf<String>()

    var onMessageToForward: ((String, Message) -> Unit)? = null
    var onMessageReceived: ((Message) -> Unit)? = null
    var onMessageRelayed: ((Message) -> Unit)? = null  // NEW: For showing relay notification

    fun updateTopology(newNodes: List<BluetoothNode>) {
        nodes.clear()
        newNodes.forEach { node ->
            nodes[node.id] = node
        }
        Log.d("MeshRouter", "Topology updated: ${nodes.size} nodes")
    }

    fun addNode(node: BluetoothNode) {
        nodes[node.id] = node
        Log.d("MeshRouter", "Added node: ${node.id.takeLast(8)}")
    }

    fun addNeighbor(nodeId: String, neighborId: String) {
        nodes[nodeId]?.neighbors?.add(neighborId)
        Log.d("MeshRouter", "Added neighbor: ${neighborId.takeLast(8)} to ${nodeId.takeLast(8)}")
    }

    fun sendMessage(receiverId: String, content: String, messageType: Int) {
        val message = Message(
            senderId = myNodeId,
            receiverId = receiverId,
            content = content,
            messageType = messageType,
            path = mutableListOf(myNodeId)
        )

        Log.d("MeshRouter", "=== SENDING MESSAGE ===")
        Log.d("MeshRouter", "From: ${myNodeId.takeLast(8)}")
        Log.d("MeshRouter", "To: ${receiverId.takeLast(8)}")
        Log.d("MeshRouter", "Content: $content")

        // Save to MY database (I'm the sender)
        saveMessageToDatabase(message, isSent = true, isReceived = false)

        // Route it
        routeMessage(message)
    }

    fun receiveMessage(message: Message) {
        Log.d("MeshRouter", "=== MESSAGE RECEIVED ===")
        Log.d("MeshRouter", "My ID: ${myNodeId.takeLast(8)}")
        Log.d("MeshRouter", "Message from: ${message.senderId.takeLast(8)}")
        Log.d("MeshRouter", "Message to: ${message.receiverId.takeLast(8)}")
        Log.d("MeshRouter", "Content: ${message.content}")
        Log.d("MeshRouter", "Hop count: ${message.hopCount}")
        Log.d("MeshRouter", "Path so far: ${message.path.map { it.takeLast(8) }}")

        // Prevent processing same message twice
        if (processedMessages.contains(message.id)) {
            Log.d("MeshRouter", "Already processed this message, ignoring")
            return
        }
        processedMessages.add(message.id)

        // Check if I'm the intended receiver
        if (message.receiverId == myNodeId) {
            // I'M THE FINAL DESTINATION!
            Log.d("MeshRouter", "✅ I'M THE RECEIVER! Message delivered!")

            // Save to MY database (I'm the receiver)
            saveMessageToDatabase(message, isSent = false, isReceived = true)

            // Notify UI to show the message
            onMessageReceived?.invoke(message)
        } else {
            // I'M A RELAY NODE (middle phone / bridge)
            Log.d("MeshRouter", "📡 I'M A RELAY NODE - Forwarding message...")

            // DON'T save to database - I'm just a bridge!
            // But notify UI to show relay indicator
            onMessageRelayed?.invoke(message)

            // Forward to next hop
            forwardMessage(message)
        }
    }

    private fun forwardMessage(message: Message) {
        // Increment hop count
        message.hopCount++
        message.path.add(myNodeId)

        Log.d("MeshRouter", "Forwarding: hop count now ${message.hopCount}")
        Log.d("MeshRouter", "Path: ${message.path.map { it.takeLast(8) }}")

        // Check if message expired (too many hops)
        if (message.hopCount > 10) {
            Log.e("MeshRouter", "Message expired (too many hops)")
            return
        }

        // Find next hop towards destination
        val nextHop = pathFinder.getNextHop(nodes, myNodeId, message.receiverId)

        if (nextHop != null) {
            Log.d("MeshRouter", "📤 Forwarding to next hop: ${nextHop.takeLast(8)}")
            onMessageToForward?.invoke(nextHop, message)
        } else {
            Log.e("MeshRouter", "❌ No route found to ${message.receiverId.takeLast(8)}")
        }
    }

    private fun routeMessage(message: Message) {
        val nextHop = pathFinder.getNextHop(nodes, myNodeId, message.receiverId)

        if (nextHop != null) {
            Log.d("MeshRouter", "Routing to next hop: ${nextHop.takeLast(8)}")
            onMessageToForward?.invoke(nextHop, message)
        } else {
            Log.e("MeshRouter", "No route found to ${message.receiverId.takeLast(8)}")
        }
    }

    private fun saveMessageToDatabase(message: Message, isSent: Boolean, isReceived: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val entity = MessageEntity(
                id = message.id,
                senderId = message.senderId,
                receiverId = message.receiverId,
                content = message.content,
                timestamp = message.timestamp,
                messageType = message.messageType,
                hopCount = message.hopCount,
                pathJson = gson.toJson(message.path),
                isSent = isSent,
                isReceived = isReceived
            )
            database.messageDao().insertMessage(entity)
            Log.d("MeshRouter", "💾 Saved to database (sent=$isSent, received=$isReceived)")
        }
    }
}