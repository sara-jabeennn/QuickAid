package com.fyp.quickaid_communication.mesh

import com.fyp.quickaid_communication.data.models.BluetoothNode
import java.util.PriorityQueue

class PathFinder {

    fun findShortestPath(
        nodes: Map<String, BluetoothNode>,
        sourceId: String,
        destinationId: String
    ): List<String>? {
        if (sourceId == destinationId) return listOf(sourceId)

        val source = nodes[sourceId] ?: return null
        val destination = nodes[destinationId] ?: return null

        // Separate mutable collections
        val distances = mutableMapOf<String, Int>()
        val visited = mutableSetOf<String>()
        val previousNodes = mutableMapOf<String, String>()

        // Initialize all distances to MAX
        nodes.keys.forEach { distances[it] = Int.MAX_VALUE }
        distances[sourceId] = 0

        // Priority queue: Pair of (nodeId, distance)
        val pq = PriorityQueue<Pair<String, Int>>(compareBy { it.second })
        pq.add(Pair(sourceId, 0))

        while (pq.isNotEmpty()) {
            val (currentId, currentDist) = pq.poll()

            if (currentId == destinationId) {
                return reconstructPath(previousNodes, sourceId, destinationId)
            }

            if (visited.contains(currentId)) continue
            visited.add(currentId)

            val currentNode = nodes[currentId] ?: continue

            for (neighborId in currentNode.neighbors) {
                if (visited.contains(neighborId)) continue

                val newDist = currentDist + 1

                if (newDist < (distances[neighborId] ?: Int.MAX_VALUE)) {
                    distances[neighborId] = newDist
                    previousNodes[neighborId] = currentId
                    pq.add(Pair(neighborId, newDist))
                }
            }
        }

        return null
    }

    private fun reconstructPath(
        previousNodes: Map<String, String>,
        sourceId: String,
        destinationId: String
    ): List<String> {
        val path = mutableListOf<String>()
        var current: String? = destinationId

        while (current != null) {
            path.add(0, current)
            if (current == sourceId) break
            current = previousNodes[current]
        }

        return path
    }

    fun getNextHop(
        nodes: Map<String, BluetoothNode>,
        currentNodeId: String,
        destinationId: String
    ): String? {
        val path = findShortestPath(nodes, currentNodeId, destinationId) ?: return null
        val currentIndex = path.indexOf(currentNodeId)
        return if (currentIndex >= 0 && currentIndex < path.size - 1) {
            path[currentIndex + 1]
        } else null
    }
}