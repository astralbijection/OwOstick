package tech.astrid.owostick.android

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.net.ProtocolException
import java.util.*

class BluetoothDeviceConnector(private val bt: BluetoothDevice) : DeviceConnector {
    override val name: String get() = bt.name
    override val drawable: Int get() = R.drawable.ic_baseline_bluetooth_24
    override fun connect(): BluetoothDeviceConnection {
        val socket = bt.createRfcommSocketToServiceRecord(BLUETOOTH_UUID)
        return BluetoothDeviceConnection(socket)
    }
}

class BluetoothDeviceConnection(private val socket: BluetoothSocket) : DeviceConnection {
    override val name: String get() = socket.remoteDevice.name
    private val writer = socket.outputStream.writer()
    private val scanner = Scanner(socket.inputStream.reader())

    override fun sendValue(value: Float) {
        writer.write(value.toString())
        val nextLine = scanner.nextLine()
        if (nextLine.trim() != "OK") {
            throw ProtocolException("Expected OK, did not get one")
        }
    }

    override fun close() {
        socket.close()
    }
}