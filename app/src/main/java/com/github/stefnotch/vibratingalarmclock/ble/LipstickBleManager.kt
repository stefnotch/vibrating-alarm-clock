package com.github.stefnotch.vibratingalarmclock.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.PhyRequest
import java.util.*


class LipstickBleManager(context: Context): BleManager(context) {
    // https://github.com/buttplugio/buttplug-rs/blob/f8d040e3c5c9db1c7038bcfb89569099e139eea6/buttplug/src/device/protocol/magic_motion_v2.rs#L49
    companion object {
        val ServiceUUID: UUID = UUID.fromString("78667579-7b48-43db-b8c5-7928a6b0a335")
        val VibrationCharacteristicUUID: UUID = UUID.fromString("78667579-a914-49a4-8333-aa3c0cd8fedc")
    }

    var vibrationCharacteristic: BluetoothGattCharacteristic? = null

    override fun getGattCallback(): BleManagerGattCallback {
        return LipstickBleManagerGattCallback()
    }

    override fun log(priority: Int, message: String) {
        // if(priority == Log.ERROR)
        Log.println(priority, "LipstickBleManager", message)
    }

    private inner class LipstickBleManagerGattCallback: BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(ServiceUUID)
            if(service != null) {
                vibrationCharacteristic = service.getCharacteristic(VibrationCharacteristicUUID)
            }

            val canWrite = (vibrationCharacteristic?.properties ?: 0) and BluetoothGattCharacteristic.PROPERTY_WRITE != 0

            return vibrationCharacteristic != null && canWrite
        }

        override fun initialize() {
            super.initialize()
            beginAtomicRequestQueue()
                .add(requestMtu(247) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                    .with { device: BluetoothDevice?, mtu: Int -> log(Log.INFO, "MTU set to $mtu") }
                    .fail { device: BluetoothDevice?, status: Int ->
                        log(
                            Log.WARN,
                            "Requested MTU not supported: $status"
                        )
                    })
                .done { device: BluetoothDevice? -> log(Log.INFO, "Target initialized") }
                .enqueue()
        }

        override fun onDeviceDisconnected() {
            vibrationCharacteristic = null
        }
    }

    fun vibrate(strength: Byte, secondValue: Byte) {
        writeCharacteristic(vibrationCharacteristic, byteArrayOf(
            0x10.toByte(),
            0xff.toByte(),
            0x04.toByte(),
            0x0a.toByte(),
            0x32.toByte(),
            0x0a.toByte(),
            0x00.toByte(),
            0x04.toByte(),
            0x08.toByte(),
            strength,
            0x64.toByte(),
            0x00.toByte(),
            0x04.toByte(),
            0x08.toByte(),
            secondValue,
            0x64.toByte(),
            0x01.toByte())
        )
            .split()
            .enqueue();
    }
}