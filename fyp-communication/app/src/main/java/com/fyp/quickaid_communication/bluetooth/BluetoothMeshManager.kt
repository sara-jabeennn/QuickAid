package com.fyp.quickaid_communication.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.fyp.quickaid_communication.utils.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothMeshManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val connectedDevices = mutableMapOf<String, BluetoothSocket>()

    var onDeviceConnected: ((String, String) -> Unit)? = null
    var onMessageReceived: ((String) -> Unit)? = null
    var onDeviceDisconnected: ((String) -> Unit)? = null
    var onConnectionStatus: ((String) -> Unit)? = null

    private var acceptThread: AcceptThread? = null

    // For controlling which devices to connect to
    private var maxConnections: Int = 10  // Default: connect to all
    private var blockedAddresses = mutableSetOf<String>()

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.BLUETOOTH_ADMIN
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun setMaxConnections(max: Int) {
        maxConnections = max
        Log.d("BT", "Max connections set to $max")
    }

    fun blockAddress(address: String) {
        blockedAddresses.add(address)
        Log.d("BT", "Blocked address: $address")
    }

    fun getConnectionCount(): Int {
        return connectedDevices.size
    }

    @SuppressLint("MissingPermission")
    fun startServer() {
        if (!hasBluetoothPermissions()) {
            Log.e("BT", "No Bluetooth permission")
            onConnectionStatus?.invoke("Missing permissions!")
            return
        }

        acceptThread = AcceptThread()
        acceptThread?.start()
        Log.d("BT", "Server started")
        onConnectionStatus?.invoke("Server started, waiting for connections...")
    }

    fun stopServer() {
        acceptThread?.cancel()
        acceptThread = null
        Log.d("BT", "Server stopped")
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
        Log.d("BT", "Discovery stopped")
        onConnectionStatus?.invoke("Discovery stopped")
    }

    @SuppressLint("MissingPermission")
    fun discoverDevices() {
        if (!hasBluetoothPermissions()) {
            Log.e("BT", "Missing permissions for discovery")
            onConnectionStatus?.invoke("Missing permissions! Please grant Location permission.")
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("BT", "Bluetooth not enabled")
            onConnectionStatus?.invoke("Please enable Bluetooth first!")
            return
        }

        bluetoothAdapter.startDiscovery()
        onConnectionStatus?.invoke("Scanning for devices...")
        Log.d("BT", "Discovery started on Android ${Build.VERSION.SDK_INT}")
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice): Boolean {
        if (!hasBluetoothPermissions()) {
            onConnectionStatus?.invoke("Missing permissions!")
            return false
        }

        // Check if we've reached max connections
        if (connectedDevices.size >= maxConnections) {
            Log.d("BT", "Max connections reached ($maxConnections), skipping ${device.name}")
            onConnectionStatus?.invoke("Max connections reached")
            return false
        }

        // Check if this address is blocked
        if (blockedAddresses.contains(device.address)) {
            Log.d("BT", "Address ${device.address} is blocked, skipping")
            return false
        }

        // Check if already connected
        if (connectedDevices.containsKey(device.address)) {
            Log.d("BT", "Already connected to ${device.address}")
            return false
        }

        onConnectionStatus?.invoke("Connecting to ${device.name}...")
        ConnectThread(device).start()
        return true
    }

    fun sendMessage(deviceAddress: String, message: String): Boolean {
        Log.d("BT", "=== SEND MESSAGE ===")
        Log.d("BT", "Target: $deviceAddress")
        Log.d("BT", "Message length: ${message.length}")
        Log.d("BT", "Available sockets: ${connectedDevices.keys.joinToString()}")

        val socket = connectedDevices[deviceAddress]
        if (socket == null) {
            Log.e("BT", "❌ No socket for device: $deviceAddress")
            onConnectionStatus?.invoke("No connection to $deviceAddress")
            return false
        }

        if (!socket.isConnected) {
            Log.e("BT", "❌ Socket not connected for $deviceAddress")
            return false
        }

        Thread {
            try {
                val outputStream: OutputStream = socket.outputStream
                val bytes = message.toByteArray()
                outputStream.write(bytes)
                outputStream.flush()
                Log.d("BT", "✅ Message sent to $deviceAddress (${bytes.size} bytes)")
            } catch (e: IOException) {
                Log.e("BT", "❌ Failed to send to $deviceAddress", e)
            }
        }.start()

        return true
    }

    fun broadcastMessage(message: String) {
        Log.d("BT", "Broadcasting to ${connectedDevices.size} devices")
        connectedDevices.forEach { (address, _) ->
            sendMessage(address, message)
        }
    }

    fun getConnectedDeviceAddresses(): List<String> {
        return connectedDevices.keys.toList()
    }

    fun isConnectedTo(address: String): Boolean {
        return connectedDevices.containsKey(address)
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    fun getMyDeviceId(): String {
        val androidId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        val formatted = androidId.take(12).chunked(2).joinToString(":")
        return formatted
    }

    @SuppressLint("MissingPermission")
    fun getMyBluetoothAddress(): String? {
        return bluetoothAdapter?.address
    }

    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket? by lazy {
            try {
                bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                    Constants.APP_NAME,
                    Constants.APP_UUID
                )
            } catch (e: SecurityException) {
                Log.e("BT", "Security exception creating server socket", e)
                null
            }
        }

        override fun run() {
            Log.d("BT", "AcceptThread running, waiting for connections...")
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    serverSocket?.accept()
                } catch (e: IOException) {
                    Log.e("BT", "Socket accept() failed", e)
                    shouldLoop = false
                    null
                }

                socket?.also {
                    val address = it.remoteDevice.address
                    Log.d("BT", "✅ Accepted incoming connection from $address")

                    // Check limits
                    if (connectedDevices.size >= maxConnections) {
                        Log.d("BT", "Max connections reached, rejecting $address")
                        try { it.close() } catch (e: Exception) {}
                    } else if (blockedAddresses.contains(address)) {
                        Log.d("BT", "Address $address is blocked, rejecting")
                        try { it.close() } catch (e: Exception) {}
                    } else if (connectedDevices.containsKey(address)) {
                        Log.d("BT", "Already connected to $address, rejecting duplicate")
                        try { it.close() } catch (e: Exception) {}
                    } else {
                        manageConnectedSocket(it)
                    }
                }
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e("BT", "Could not close server socket", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {

        override fun run() {
            bluetoothAdapter?.cancelDiscovery()

            // Check if already connected
            if (connectedDevices.containsKey(device.address)) {
                Log.d("BT", "Already connected to ${device.address}, skipping")
                return
            }

            var connected = false
            var socket: BluetoothSocket? = null

            // Method 1: Standard UUID-based connection
            if (!connected) {
                try {
                    Log.d("BT", "Trying Method 1: Standard RFCOMM for ${device.name}...")
                    onConnectionStatus?.invoke("Trying standard connection...")

                    socket = device.createRfcommSocketToServiceRecord(Constants.APP_UUID)
                    socket.connect()
                    connected = true
                    Log.d("BT", "✅ Connected using Method 1: Standard RFCOMM")
                    onConnectionStatus?.invoke("Connected using standard method!")
                } catch (e: IOException) {
                    Log.e("BT", "Method 1 failed: ${e.message}")
                    try { socket?.close() } catch (ce: Exception) {}
                    socket = null
                }
            }

            // Method 2: Insecure RFCOMM
            if (!connected) {
                try {
                    Log.d("BT", "Trying Method 2: Insecure RFCOMM...")
                    onConnectionStatus?.invoke("Trying insecure connection...")

                    socket = device.createInsecureRfcommSocketToServiceRecord(Constants.APP_UUID)
                    socket.connect()
                    connected = true
                    Log.d("BT", "✅ Connected using Method 2: Insecure RFCOMM")
                    onConnectionStatus?.invoke("Connected using insecure method!")
                } catch (e: IOException) {
                    Log.e("BT", "Method 2 failed: ${e.message}")
                    try { socket?.close() } catch (ce: Exception) {}
                    socket = null
                }
            }

            // Method 3: Reflection fallback (for older devices)
            if (!connected) {
                try {
                    Log.d("BT", "Trying Method 3: Reflection fallback (channel 1)...")
                    onConnectionStatus?.invoke("Trying fallback for older devices...")

                    socket = device.javaClass.getMethod(
                        "createRfcommSocket", Int::class.javaPrimitiveType
                    ).invoke(device, 1) as BluetoothSocket

                    socket.connect()
                    connected = true
                    Log.d("BT", "✅ Connected using Method 3: Reflection fallback")
                    onConnectionStatus?.invoke("Connected using fallback method!")
                } catch (e: Exception) {
                    Log.e("BT", "Method 3 failed: ${e.message}")
                    try { socket?.close() } catch (ce: Exception) {}
                    socket = null
                }
            }

            // Method 4: Try different RFCOMM channels (2-5)
            if (!connected) {
                for (channel in 2..5) {
                    try {
                        Log.d("BT", "Trying Method 4: Channel $channel...")
                        onConnectionStatus?.invoke("Trying channel $channel...")

                        socket = device.javaClass.getMethod(
                            "createRfcommSocket", Int::class.javaPrimitiveType
                        ).invoke(device, channel) as BluetoothSocket

                        socket.connect()
                        connected = true
                        Log.d("BT", "✅ Connected using Method 4: Channel $channel")
                        onConnectionStatus?.invoke("Connected using channel $channel!")
                        break
                    } catch (e: Exception) {
                        Log.e("BT", "Channel $channel failed: ${e.message}")
                        try { socket?.close() } catch (ce: Exception) {}
                        socket = null
                    }
                }
            }

            // Handle result
            if (connected && socket != null) {
                manageConnectedSocket(socket)
            } else {
                Log.e("BT", "❌ All connection methods failed for ${device.name}")
                onConnectionStatus?.invoke("Failed to connect to ${device.name}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun manageConnectedSocket(socket: BluetoothSocket) {
        val deviceAddress = socket.remoteDevice.address
        val deviceName = socket.remoteDevice.name ?: "Unknown"

        Log.d("BT", "=== MANAGING SOCKET ===")
        Log.d("BT", "Device: $deviceName")
        Log.d("BT", "Address: $deviceAddress")
        Log.d("BT", "Connected: ${socket.isConnected}")

        // Avoid duplicates
        if (connectedDevices.containsKey(deviceAddress)) {
            Log.d("BT", "Already have socket for $deviceAddress, closing duplicate")
            try { socket.close() } catch (e: Exception) {}
            return
        }

        connectedDevices[deviceAddress] = socket
        Log.d("BT", "✅ Stored socket. Total connections: ${connectedDevices.size}")
        Log.d("BT", "All connected: ${connectedDevices.keys.joinToString()}")

        onDeviceConnected?.invoke(deviceAddress, deviceName)
        onConnectionStatus?.invoke("Connected to $deviceName")

        ConnectedThread(socket, deviceAddress).start()
    }

    private inner class ConnectedThread(
        private val socket: BluetoothSocket,
        private val deviceAddress: String
    ) : Thread() {
        private val inputStream: InputStream = socket.inputStream
        private val buffer: ByteArray = ByteArray(4096)  // Larger buffer

        override fun run() {
            Log.d("BT", "Started listening thread for $deviceAddress")
            var numBytes: Int

            while (true) {
                numBytes = try {
                    inputStream.read(buffer)
                } catch (e: IOException) {
                    Log.e("BT", "Input stream disconnected for $deviceAddress", e)
                    connectionLost()
                    break
                }

                if (numBytes > 0) {
                    val message = String(buffer, 0, numBytes)
                    Log.d("BT", "📩 Received $numBytes bytes from $deviceAddress")
                    Log.d("BT", "Content preview: ${message.take(100)}")
                    onMessageReceived?.invoke(message)
                }
            }
        }

        private fun connectionLost() {
            Log.d("BT", "Connection lost to $deviceAddress")
            connectedDevices.remove(deviceAddress)
            onDeviceDisconnected?.invoke(deviceAddress)
            onConnectionStatus?.invoke("Disconnected from $deviceAddress")
        }
    }
}