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
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.astrid.owostick.android.databinding.FragmentDeviceConnectionBinding
import tech.astrid.owostick.android.databinding.FragmentServerConnectionBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DeviceConnection.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeviceConnectionFragment : Fragment() {
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

        with (binding) {
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

            state.subscribe {
                requireActivity().runOnUiThread {
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
        with (binding) {
            deviceList.adapter = arrayAdapter
        }
    }

    private lateinit var binding: FragmentDeviceConnectionBinding
    private val state = BehaviorSubject.createDefault<ConnectingState>(ConnectingState.Disconnected)

    sealed class ConnectingState {
        object Disconnected : ConnectingState()
        data class Connecting(val connector: DeviceConnector) : ConnectingState()
        data class Connected(val connection: DeviceConnection) : ConnectingState()
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