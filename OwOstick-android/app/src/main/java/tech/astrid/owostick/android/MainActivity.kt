package tech.astrid.owostick.android

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {
    private val tag = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectToDeviceButton.setOnClickListener {
            when (val stateObj = state.value) {
                is ConnectingState.Disconnected -> {
                    val connector = deviceList.selectedItem as DeviceConnector
                    Log.i(tag, "Connecting")

                    state.onNext(ConnectingState.Connecting(connector))

                    lifecycleScope.launch(Dispatchers.IO) {
                        state.onNext(ConnectingState.Connected(connector.connect()))
                        Log.i(tag, "Connected")
                    }
                }
                is ConnectingState.Connected -> {
                    stateObj.connection.sendValue(0.0f)
                    stateObj.connection.close()
                    state.onNext(ConnectingState.Disconnected)
                }
                is ConnectingState.Connecting -> {
                    throw IllegalStateException("Button cannot be clicked while still connecting!")
                }
            }
        }

        powerSlider.valueSubject
            .subscribe {
                when (val state = state.value) {
                    is ConnectingState.Connected -> state.connection.sendValue(it)
                }
            }

        state.subscribe {
            runOnUiThread {
                deviceList.isEnabled = it is ConnectingState.Disconnected
                connectToDeviceButton.isEnabled = it !is ConnectingState.Connecting
                connectToDeviceButton.text = when (it) {
                    is ConnectingState.Disconnected -> "Connect"
                    is ConnectingState.Connecting -> "Connecting..."
                    is ConnectingState.Connected -> "Disconnect"
                }
            }
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
        val devices: List<DeviceConnector> = pairedDevices!!.map { BluetoothDeviceConnector(it) }

        val arrayAdapter = DeviceAdapter(this, devices)
        deviceList.adapter = arrayAdapter
    }

    private val state = BehaviorSubject.createDefault<ConnectingState>(ConnectingState.Disconnected)

    sealed class ConnectingState {
        object Disconnected : ConnectingState()
        data class Connecting(val connector: DeviceConnector) : ConnectingState()
        data class Connected(val connection: DeviceConnection) : ConnectingState()
    }
}
