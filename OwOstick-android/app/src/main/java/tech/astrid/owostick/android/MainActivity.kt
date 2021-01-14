package tech.astrid.owostick.android

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectButton.setOnClickListener {
            doConnect()
        }
    }

    override fun onStart() {
        super.onStart()

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        val devices: List<DeviceConnector> = pairedDevices?.map { it -> BluetoothDeviceConnector(it) as DeviceConnector }!!

        val arrayAdapter = DeviceAdapter(this, devices)
        deviceList.adapter = arrayAdapter
    }

    private fun doConnect() {
        val item = deviceList.selectedItem as DeviceConnector
        deviceList.isEnabled = false
        connectButton.isEnabled = false
        connection = item.connect()
        connectButton.isEnabled = true
    }

    var connection: DeviceConnection? = null
}