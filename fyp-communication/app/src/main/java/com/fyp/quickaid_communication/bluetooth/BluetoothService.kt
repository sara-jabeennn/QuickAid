package com.fyp.quickaid_communication.bluetooth

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class BluetoothService : Service() {

    private val binder = LocalBinder()
    private var bluetoothManager: BluetoothMeshManager? = null

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BluetoothService", "Service created")
        bluetoothManager = BluetoothMeshManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BluetoothService", "Service started")
        bluetoothManager?.startServer()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BluetoothService", "Service destroyed")
        bluetoothManager?.stopServer()
    }

    fun getBluetoothManager(): BluetoothMeshManager? {
        return bluetoothManager
    }
}