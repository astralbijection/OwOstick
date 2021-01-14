package tech.astrid.owostick.android

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.net.ProtocolException
import java.util.*

class BluetoothDeviceConnector(private val bt: BluetoothDevice) : DeviceConnector {
    private val tag = javaClass.name

    override val name: String get() = bt.name
    override val drawable: Int get() = R.drawable.ic_baseline_bluetooth_24

    override fun connect(): BluetoothDeviceConnection {
        Log.d(tag, "Connecting to device ${bt.name}")
        val socket = bt.createInsecureRfcommSocketToServiceRecord(BLUETOOTH_UUID)
        val connection = BluetoothDeviceConnection(socket)
        connection.sendValue(0.0f)
        return connection
    }
}

class BluetoothDeviceConnection(private val socket: BluetoothSocket) : DeviceConnection {
    override val name: String get() = socket.remoteDevice.name
    private val writer = socket.outputStream.writer()
    private val scanner = Scanner(socket.inputStream.reader())

    private val tag = javaClass.name

    init {
        socket.connect()
        expect("READY")
    }

    override fun sendValue(value: Float) {
        send(value.toString())
        expect("OK")
    }

    private fun send(str: String) {
        Log.v(tag, "sending $str")
        writer.write(str)
        writer.write("\n")
        writer.flush()
        Log.v(tag, "sent $str")
    }

    private fun expect(str: String) {
        Log.v(tag, "expecting $str")
        val nextLine = scanner.nextLine().trim()
        if (nextLine != str) {
            throw ProtocolException("Expected $str, got $nextLine")
        }
    }

    override fun close() {
        socket.close()
    }
}