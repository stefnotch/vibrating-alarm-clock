package com.github.stefnotch.vibratingalarmclock.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import no.nordicsemi.android.ble.observer.ConnectionObserver

class BleConnection : ConnectionObserver {
    private var manager: LipstickBleManager? = null

    fun connect(device: BluetoothDevice, context: Context) {
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