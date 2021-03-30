package com.github.stefnotch.vibratingalarmclock.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import no.nordicsemi.android.ble.observer.ConnectionObserver

class BleConnection : ConnectionObserver {
    private var manager: LipstickBleManager? = null
    private var isScanning = false
    private var isVibrating = false
    private val scanCallback = object :ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if(result != null) {
                scanResults?.set(result.device.address, result)
                onChangeScanResults()
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            if(results != null) {
                results.forEach {
                    scanResults?.set(it.device.address, it)
                }
                onChangeScanResults()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            onChangeScanResults(errorCode)
            stopScanning()
        }
    }
    private var scanResults: MutableMap<String, ScanResult>? = null
    private var scanResultsCallback: (List<String>) -> Unit = {  }

    companion object {
        private var instance: BleConnection? = null

        fun getInstance(): BleConnection {
            return instance ?: synchronized(this) {
                instance ?: BleConnection().also{ instance = it }
            }
        }
    }

    private fun onChangeScanResults(errorCode: Int? = null) {
        if(errorCode == null) {
            scanResultsCallback(scanResults?.map { (k, v) -> v.scanRecord?.deviceName ?: "" } ?: listOf(""))
        } else {
            scanResultsCallback(listOf("An error occurred while scanning: $errorCode"))
        }
    }

    fun startScanning(callback: (List<String>) -> Unit) {
        if(isScanning) return
        isScanning = true

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        scanResults = mutableMapOf()
        scanResultsCallback = callback
        onChangeScanResults()
        // TODO: Figure out ideal scan settings
        bluetoothAdapter.bluetoothLeScanner.startScan(null, ScanSettings.Builder().setReportDelay(500).build(), scanCallback)

        // Automatically stop scanning after a while
        Handler(Looper.getMainLooper()).postDelayed({
            stopScanning()
        }, 1000 * 20)
    }

    fun stopScanning() {
        if(!isScanning) return

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)

        scanResults = null
        scanResultsCallback = { }
        isScanning = false // TODO: Probably notify the UI of this
    }

    fun connectToLipstick(context: Context): Boolean {
        val results = scanResults
        stopScanning()
        if(results != null) {
            // TODO: Maybe there is a better way of doing this
            val lipstick = results.values.find { result -> result.isConnectable && (result.scanRecord?.deviceName?.equals("Lipstick", true) == true) }?.device
            if(lipstick != null) {
                connect(lipstick, context)
                return true
            }
        }

        return false
    }

    fun startVibrating() {
        isVibrating = true
        // TODO: Make this rock-solid
        var vibrationRunnable: Runnable? = null

        vibrationRunnable = Runnable {
            if(isVibrating) {
                var strength1 = (Math.random() * 0xff).toInt().toByte()
                val strength2 = (Math.random() * 0xff).toInt().toByte()
                if (strength1 < 0x70) {
                    strength1 = 0x70
                }
                manager?.vibrate(strength1, strength2)
                Handler(Looper.getMainLooper()).postDelayed({
                    manager?.vibrate(0, 0)
                }, 2000)

                vibrationRunnable?.let {
                    Handler(Looper.getMainLooper()).postDelayed(
                        it,
                        4500 + (Math.random() * 4000).toLong()
                    )
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed(vibrationRunnable, 1000)
    }

    fun stopVibrating() {
        isVibrating = false
        manager?.vibrate(0,0)
    }

    private fun connect(device: BluetoothDevice, context: Context) {
        // Doesn't seem to work with this device
        // device.createBond() // https://github.com/NordicSemiconductor/Android-BLE-Library/issues/265

        manager = LipstickBleManager(context)
        manager?.setConnectionObserver(this)
        manager?.connect(device)
            ?.timeout(1000 * 100)
            ?.retry(3, 200)
            ?.done { /*callback*/ }
            ?.useAutoConnect(true)
            ?.enqueue()
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        // TODO: Callback here
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
    }
}