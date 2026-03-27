package com.fyp.quickaid_communication.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.fyp.quickaid_communication.R
import com.fyp.quickaid_communication.bluetooth.BluetoothMeshManager
import com.fyp.quickaid_communication.data.database.AppDatabase
import com.fyp.quickaid_communication.data.models.BluetoothNode
import com.fyp.quickaid_communication.data.models.Message
import com.fyp.quickaid_communication.mesh.MeshRouter
import com.fyp.quickaid_communication.utils.Constants
import com.fyp.quickaid_communication.utils.NetworkUtils
import kotlinx.coroutines.launch

class MeshChatActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvDeviceCount: TextView
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var fabStartMesh: FloatingActionButton

    private lateinit var bluetoothManager: BluetoothMeshManager
    private lateinit var meshRouter: MeshRouter
    private lateinit var database: AppDatabase
    private lateinit var messageAdapter: MessageAdapter

    private val gson = Gson()
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    private val connectedNodes = mutableMapOf<String, BluetoothNode>()
    private val realIdToBluetoothAddress = mutableMapOf<String, String>()

    // ALL nodes in the mesh (including ones we're not directly connected to)
    private val allMeshNodes = mutableMapOf<String, BluetoothNode>()

    private val PERMISSION_REQUEST_CODE = 1001
    private var isBluetoothInitialized = false
    private var myHasInternet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mesh_chat)

        initViews()
        initDatabase()
        setupRecyclerView()
        setupClickListeners()
        observeMessages()

        myHasInternet = NetworkUtils.hasInternetConnection(this)
        requestBluetoothPermissions()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        tvDeviceCount = findViewById(R.id.tvDeviceCount)
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        fabStartMesh = findViewById(R.id.fabStartMesh)
        tvStatus.text = "Status: Initializing..."
    }

    private fun initDatabase() {
        database = AppDatabase.getDatabase(this)
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            initializeBluetoothComponents()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            initializeBluetoothComponents()
        } else {
            tvStatus.text = "Status: Permissions denied"
        }
    }

    private fun initializeBluetoothComponents() {
        initBluetooth()
        initMeshRouter()
        isBluetoothInitialized = true

        val status = if (myHasInternet) "📶 GATEWAY (Has Internet)" else "📵 NO INTERNET"
        tvStatus.text = "Ready\n$status\nTap button to join mesh"
        updateUI()
    }

    private fun initBluetooth() {
        bluetoothManager = BluetoothMeshManager(this)

        bluetoothManager.onDeviceConnected = { bluetoothAddress, deviceName ->
            runOnUiThread {
                Toast.makeText(this, "✅ $deviceName", Toast.LENGTH_SHORT).show()
                Log.d("MESH", "BT Connected: $bluetoothAddress ($deviceName)")

                // Send my ID AND my topology to the connected device
                val myRealId = bluetoothManager.getMyDeviceId()
                val idExchange = mapOf(
                    "type" to "ID_EXCHANGE",
                    "realId" to myRealId,
                    "name" to Build.MODEL,
                    "hasInternet" to myHasInternet,
                    "knownNodes" to allMeshNodes.map {
                        mapOf(
                            "id" to it.key,
                            "name" to it.value.name,
                            "hasInternet" to it.value.hasInternet,
                            "neighbors" to it.value.neighbors.toList()
                        )
                    }
                )
                bluetoothManager.sendMessage(bluetoothAddress, gson.toJson(idExchange))

                // Store temporarily with BT address
                connectedNodes[bluetoothAddress] = BluetoothNode(bluetoothAddress, deviceName, false)
                realIdToBluetoothAddress[bluetoothAddress] = bluetoothAddress
                updateUI()
            }
        }

        bluetoothManager.onMessageReceived = { messageJson ->
            Log.d("MESH", "📩 Received: ${messageJson.take(100)}")
            try {
                when {
                    messageJson.contains("\"type\":\"ID_EXCHANGE\"") -> handleIdExchange(messageJson)
                    messageJson.contains("\"type\":\"TOPOLOGY_UPDATE\"") -> handleTopologyUpdate(messageJson)
                    else -> handleMeshMessage(messageJson)
                }
            } catch (e: Exception) {
                Log.e("MESH", "Error: ${e.message}")
                e.printStackTrace()
            }
        }

        bluetoothManager.onDeviceDisconnected = { deviceId ->
            runOnUiThread {
                connectedNodes.remove(deviceId)
                realIdToBluetoothAddress.entries.removeIf { it.value == deviceId || it.key == deviceId }
                updateUI()
            }
        }
    }

    private fun handleIdExchange(json: String) {
        val idData = gson.fromJson(json, Map::class.java) as Map<String, Any>
        val realId = idData["realId"] as String
        val deviceName = idData["name"] as? String ?: "Device"
        val hasInternet = idData["hasInternet"] as? Boolean ?: false
        val knownNodes = idData["knownNodes"] as? List<Map<String, Any>> ?: emptyList()

        Log.d("MESH", "ID Exchange: $realId, internet=$hasInternet, knows ${knownNodes.size} nodes")

        runOnUiThread {
            // Find unmapped BT address
            val btAddress = connectedNodes.keys.firstOrNull { key ->
                key.length == 17 && key.count { it == ':' } == 5 && realIdToBluetoothAddress[key] == key
            }

            if (btAddress != null) {
                Log.d("MESH", "Mapping $realId → $btAddress")

                realIdToBluetoothAddress.remove(btAddress)
                realIdToBluetoothAddress[realId] = btAddress

                connectedNodes.remove(btAddress)
                val node = BluetoothNode(realId, deviceName, hasInternet)
                connectedNodes[realId] = node

                // Add to mesh router
                meshRouter.addNode(node)
                val myId = bluetoothManager.getMyDeviceId()
                meshRouter.addNeighbor(myId, realId)
                node.neighbors.add(myId)

                // Add to our known mesh nodes
                allMeshNodes[realId] = node

                // Process the nodes they know about
                processKnownNodes(knownNodes, realId)

                if (hasInternet) {
                    Toast.makeText(this, "🌐 Gateway found: ${realId.takeLast(8)}", Toast.LENGTH_LONG).show()
                }

                // Share our updated topology with all connected devices
                broadcastTopologyUpdate()

                updateUI()
            }
        }
    }

    private fun processKnownNodes(knownNodes: List<Map<String, Any>>, sourceId: String) {
        Log.d("MESH", "Processing ${knownNodes.size} known nodes from $sourceId")

        for (nodeData in knownNodes) {
            val nodeId = nodeData["id"] as? String ?: continue
            val nodeName = nodeData["name"] as? String ?: "Device"
            val nodeHasInternet = nodeData["hasInternet"] as? Boolean ?: false
            val neighbors = (nodeData["neighbors"] as? List<String>) ?: emptyList()

            // Skip if it's our own ID
            if (nodeId == bluetoothManager.getMyDeviceId()) continue

            // Check if we already know this node
            if (!allMeshNodes.containsKey(nodeId)) {
                Log.d("MESH", "Discovered new node: $nodeId (via $sourceId)")

                val newNode = BluetoothNode(nodeId, nodeName, nodeHasInternet)
                newNode.neighbors.addAll(neighbors)

                allMeshNodes[nodeId] = newNode
                meshRouter.addNode(newNode)

                // This node is reachable through sourceId
                meshRouter.addNeighbor(sourceId, nodeId)

                if (nodeHasInternet) {
                    runOnUiThread {
                        Toast.makeText(this, "🌐 Discovered gateway: ${nodeId.takeLast(8)}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                // Update existing node info - create new node with updated info
                val existingNode = allMeshNodes[nodeId]!!
                val updatedNode = BluetoothNode(nodeId, nodeName, nodeHasInternet)
                updatedNode.neighbors.addAll(existingNode.neighbors)
                updatedNode.neighbors.addAll(neighbors)
                allMeshNodes[nodeId] = updatedNode
                meshRouter.addNode(updatedNode)
            }
        }

        Log.d("MESH", "Now know ${allMeshNodes.size} total nodes in mesh")
    }

    private fun handleTopologyUpdate(json: String) {
        val updateData = gson.fromJson(json, Map::class.java) as Map<String, Any>
        val sourceId = updateData["sourceId"] as String
        val nodes = updateData["nodes"] as? List<Map<String, Any>> ?: emptyList()

        Log.d("MESH", "Topology update from $sourceId with ${nodes.size} nodes")

        runOnUiThread {
            processKnownNodes(nodes, sourceId)
            updateUI()
        }
    }

    private fun broadcastTopologyUpdate() {
        val myId = bluetoothManager.getMyDeviceId()
        val update = mapOf(
            "type" to "TOPOLOGY_UPDATE",
            "sourceId" to myId,
            "nodes" to allMeshNodes.map {
                mapOf(
                    "id" to it.key,
                    "name" to it.value.name,
                    "hasInternet" to it.value.hasInternet,
                    "neighbors" to it.value.neighbors.toList()
                )
            }
        )

        val json = gson.toJson(update)
        Log.d("MESH", "Broadcasting topology: ${allMeshNodes.size} nodes")

        // Send to all directly connected devices
        for ((realId, btAddress) in realIdToBluetoothAddress) {
            if (realId != btAddress) {  // Only send to mapped devices
                bluetoothManager.sendMessage(btAddress, json)
            }
        }
    }

    private fun handleMeshMessage(json: String) {
        val message = gson.fromJson(json, Message::class.java)
        Log.d("MESH", "Mesh msg: ${message.senderId.takeLast(8)} → ${message.receiverId.takeLast(8)}")
        meshRouter.receiveMessage(message)
    }

    private fun initMeshRouter() {
        val myNodeId = bluetoothManager.getMyDeviceId()
        meshRouter = MeshRouter(database, myNodeId)

        val myNode = BluetoothNode(myNodeId, Build.MODEL, myHasInternet)
        meshRouter.addNode(myNode)
        allMeshNodes[myNodeId] = myNode

        meshRouter.onMessageToForward = { nextHopId, message ->
            Log.d("MESH", "Forwarding to next hop: $nextHopId")

            val btAddress = realIdToBluetoothAddress[nextHopId]
            if (btAddress != null) {
                bluetoothManager.sendMessage(btAddress, gson.toJson(message))
                runOnUiThread {
                    Toast.makeText(this, "📤 → ${nextHopId.takeLast(8)}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("MESH", "No direct connection to $nextHopId, checking if reachable...")

                // Try to find a path through other nodes
                val actualNextHop = findNextHopTo(nextHopId)
                if (actualNextHop != null && realIdToBluetoothAddress.containsKey(actualNextHop)) {
                    val actualBtAddress = realIdToBluetoothAddress[actualNextHop]!!
                    bluetoothManager.sendMessage(actualBtAddress, gson.toJson(message))
                    runOnUiThread {
                        Toast.makeText(this, "📤 → ${actualNextHop.takeLast(8)} (routing to ${nextHopId.takeLast(8)})", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "❌ No route to ${nextHopId.takeLast(8)}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        meshRouter.onMessageReceived = { message ->
            runOnUiThread {
                val from = message.senderId.takeLast(8)
                val hops = message.hopCount
                val path = message.path.map { it.takeLast(8) }.joinToString(" → ")

                Toast.makeText(this, "✅ From $from: ${message.content}", Toast.LENGTH_LONG).show()
                tvStatus.text = "📥 RECEIVED\nFrom: $from\n${message.content}\nHops: $hops\nPath: $path"

                if (myHasInternet) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        Toast.makeText(this, "🌐 Forwarding to authorities...", Toast.LENGTH_LONG).show()
                    }, 1000)
                }
            }
        }

        meshRouter.onMessageRelayed = { message ->
            runOnUiThread {
                val from = message.senderId.takeLast(8)
                val to = message.receiverId.takeLast(8)
                Toast.makeText(this, "📡 Relaying: $from → $to (Hop #${message.hopCount})", Toast.LENGTH_LONG).show()
                tvStatus.text = "📡 RELAYING\n$from → $to\nHop #${message.hopCount}\nI am the bridge!"
                Handler(Looper.getMainLooper()).postDelayed({ updateUI() }, 3000)
            }
        }
    }

    private fun findNextHopTo(destinationId: String): String? {
        // Find which of our direct connections can reach the destination
        for ((realId, _) in realIdToBluetoothAddress) {
            if (realId.length == 17 && realId.count { it == ':' } == 5) continue  // Skip unmapped

            val node = allMeshNodes[realId]
            if (node != null && node.neighbors.contains(destinationId)) {
                return realId
            }
        }
        return null
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.adapter = messageAdapter
    }

    private fun setupClickListeners() {
        fabStartMesh.setOnClickListener {
            if (isBluetoothInitialized) startMeshNetwork()
        }

        btnSend.setOnClickListener {
            if (isBluetoothInitialized) sendEmergencyMessage()
        }

        tvDeviceCount.setOnLongClickListener {
            showDebugInfo()
            true
        }
    }

    private fun startMeshNetwork() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !isLocationEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("Location Required")
                .setMessage("Turn on GPS for Bluetooth scanning")
                .setPositiveButton("Settings") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivity(discoverableIntent)

        bluetoothManager.startServer()
        bluetoothManager.discoverDevices()
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        tvStatus.text = "Scanning for nearby devices..."
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_FOUND) {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }

                device?.let {
                    if (hasBluetoothPermission() && !discoveredDevices.contains(it)) {
                        discoveredDevices.add(it)
                        bluetoothManager.connectToDevice(it)
                    }
                }
            }
        }
    }

    private fun sendEmergencyMessage() {
        val content = etMessage.text.toString().trim()
        if (content.isEmpty()) {
            Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        if (connectedNodes.isEmpty() && allMeshNodes.size <= 1) {
            Toast.makeText(this, "No devices in mesh", Toast.LENGTH_SHORT).show()
            return
        }

        // Find the GATEWAY in ALL known nodes (not just direct connections!)
        val gateway = allMeshNodes.values.find { it.hasInternet && it.id != bluetoothManager.getMyDeviceId() }

        val receiverId = if (gateway != null) {
            Toast.makeText(this, "🌐 Sending to GATEWAY: ${gateway.id.takeLast(8)}", Toast.LENGTH_SHORT).show()
            gateway.id
        } else if (connectedNodes.isNotEmpty()) {
            Toast.makeText(this, "⚠️ No gateway found, sending to first device", Toast.LENGTH_SHORT).show()
            connectedNodes.keys.first()
        } else {
            Toast.makeText(this, "No devices to send to", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("MESH", "Sending to: $receiverId (Gateway: ${gateway != null})")
        tvStatus.text = "📤 Sending to ${receiverId.takeLast(8)}..."

        meshRouter.sendMessage(receiverId, content, Constants.MESSAGE_TYPE_EMERGENCY)
        etMessage.setText("")
    }

    private fun showDebugInfo() {
        val myId = bluetoothManager.getMyDeviceId()
        val directConnections = connectedNodes.map { "${it.key.takeLast(8)}(${if (it.value.hasInternet) "📶" else "📵"})" }
        val allNodes = allMeshNodes.map { "${it.key.takeLast(8)}(${if (it.value.hasInternet) "📶" else "📵"})" }
        val mappings = realIdToBluetoothAddress.filter { it.key != it.value }.map { "${it.key.takeLast(8)}→${it.value.takeLast(8)}" }

        val info = """
MY ID: ${myId.takeLast(8)}
INTERNET: $myHasInternet

DIRECT CONNECTIONS (${connectedNodes.size}):
${directConnections.joinToString("\n")}

ALL MESH NODES (${allMeshNodes.size}):
${allNodes.joinToString("\n")}

ID→BT MAPPINGS:
${mappings.joinToString("\n")}

GATEWAY: ${allMeshNodes.values.find { it.hasInternet }?.id?.takeLast(8) ?: "None"}
        """.trimIndent()

        AlertDialog.Builder(this).setTitle("Debug").setMessage(info).setPositiveButton("OK", null).show()
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            database.messageDao().getAllMessages().collect { messages ->
                messageAdapter.submitList(messages)
            }
        }
    }

    private fun updateUI() {
        val myId = bluetoothManager.getMyDeviceId().takeLast(8)
        val myStatus = if (myHasInternet) "📶 GATEWAY" else "📵"

        val directNodes = connectedNodes.map { "${if (it.value.hasInternet) "📶" else "📵"}${it.key.takeLast(8)}" }.joinToString(", ")

        val hasGateway = myHasInternet || allMeshNodes.values.any { it.hasInternet }
        val gatewayInfo = allMeshNodes.values.find { it.hasInternet }?.id?.takeLast(8) ?: "None"

        tvDeviceCount.text = """
My ID: $myId ($myStatus)
Direct: ${connectedNodes.size} | Mesh: ${allMeshNodes.size}
Nodes: $directNodes
${if (hasGateway) "✅ Gateway: $gatewayInfo" else "⚠️ No gateway"}
(Long press for debug)
        """.trimIndent()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(receiver) } catch (e: Exception) {}
        if (isBluetoothInitialized) bluetoothManager.stopServer()
    }
}