package tech.astrid.owostick.android

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.astrid.owostick.android.databinding.FragmentDeviceConnectionBinding


class DeviceConnectionFragment : Fragment() {
    private lateinit var binding: FragmentDeviceConnectionBinding
    private val _state = BehaviorSubject.createDefault<State>(State.Disconnected)
    val state: Observable<State> get() = _state

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceConnectionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            connectToDeviceButton.setOnClickListener {
                when (val stateObj = _state.value) {
                    is State.Disconnected -> {
                        val connector = deviceList.selectedItem as DeviceConnector
                        Log.i(tag, "Connecting")

                        _state.onNext(State.Connecting(connector))

                        lifecycleScope.launch(Dispatchers.IO) {
                            _state.onNext(State.Connected(connector.connect()))
                            Log.i(tag, "Connected")
                        }
                    }
                    is State.Connected -> {
                        stateObj.connection.sendValue(0.0f)
                        stateObj.connection.close()
                        _state.onNext(State.Disconnected)
                    }
                    is State.Connecting -> {
                        throw IllegalStateException("Button cannot be clicked while still connecting!")
                    }
                }
            }

            _state.subscribe {
                requireActivity().runOnUiThread {
                    deviceList.isEnabled = it is State.Disconnected
                    connectToDeviceButton.isEnabled = it !is State.Connecting
                    connectToDeviceButton.text = when (it) {
                        is State.Disconnected -> "Connect"
                        is State.Connecting -> "Connecting..."
                        is State.Connected -> "Disconnect"
                    }
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

        val arrayAdapter = DeviceAdapter(requireActivity(), devices)
        with(binding) {
            deviceList.adapter = arrayAdapter
        }
    }

    sealed class State {
        object Disconnected : State()
        data class Connecting(val connector: DeviceConnector) : State()
        data class Connected(val connection: DeviceConnection) : State()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DeviceConnection.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = DeviceConnectionFragment()
    }
}