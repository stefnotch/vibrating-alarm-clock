package com.github.stefnotch.vibratingalarmclock.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.github.stefnotch.vibratingalarmclock.data.AppDatabase
import no.nordicsemi.android.ble.observer.ConnectionObserver

class BleConnection : ConnectionObserver {
    private var manager: LipstickBleManager? = null
    private var isScanning = false
    private val scanCallback = object :ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if(result != null) {
                scanResults?.add(result)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            if(results != null) {
                scanResults?.addAll(results)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // TODO: Handle this
            stopScanning()
        }
    }
    private var scanResults: MutableList<ScanResult>? = null

    companion object {
        private var instance: BleConnection? = null

        fun getInstance(): BleConnection {
            return instance ?: synchronized(this) {
                instance ?: BleConnection().also{ instance = it }
            }
        }
    }

    fun startScanning() {
        if(isScanning) return
        isScanning = true

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        scanResults = mutableListOf()
        // TODO: Figure out ideal scan settings
        bluetoothAdapter.bluetoothLeScanner.startScan(null, ScanSettings.Builder().setReportDelay(500).build(), scanCallback)

        // TODO: Automatically stop scanning after a while
    }

    fun stopScanning() {
        if(!isScanning) return

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)

        scanResults = null
        isScanning = false
    }

    fun connectToLipstick(context: Context): Boolean {
        val results = scanResults
        stopScanning()
        if(results != null) {
            // TODO: Maybe there is a better way of doing this
            val lipstick = results?.find { result -> result.isConnectable && (result.scanRecord?.deviceName?.equals("Lipstick", true) == true) }?.device
            if(lipstick != null) {
                connect(lipstick, context)
                return true
            }
        }

        return false
    }

    fun startVibrating() {
        Handler(Looper.getMainLooper()).postDelayed({
            var strength1 = (Math.random() * 0xff).toInt().toByte()
            var strength2 = (Math.random() * 0xff).toInt().toByte()
            if(strength1 < 0x70) {
                strength1 = 0x70
            }
            manager?.vibrate(strength1, strength2)
        }, 1000)
    }

    fun stopVibrating() {
        manager?.vibrate(0,0)
    }

    private fun connect(device: BluetoothDevice, context: Context) {
        device.createBond() // https://github.com/NordicSemiconductor/Android-BLE-Library/issues/265

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