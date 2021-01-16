package tech.astrid.owostick.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.findFragment
import io.reactivex.rxjava3.kotlin.Observables.combineLatest
import tech.astrid.owostick.android.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val tag = javaClass.name

    lateinit var binding: ActivityMainBinding
    lateinit var deviceFragment: DeviceConnectionFragment
    lateinit var serverFragment: ServerConnectionFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        serverFragment = supportFragmentManager.findFragmentById(R.id.serverFragmentContainer) as ServerConnectionFragment
        deviceFragment = supportFragmentManager.findFragmentById(R.id.deviceFragmentContainer) as DeviceConnectionFragment

        val inputSource = serverFragment.state
            .switchMap {
                if (it is ServerConnectionFragment.State.Connected) {
                    Log.i(tag, "Using server")
                    serverFragment.power
                } else {
                    Log.i(tag, "Using slider ${binding.powerSlider.valueSubject}")
                    binding.powerSlider.valueSubject
                }
            }

        combineLatest(deviceFragment.state, inputSource)
            .subscribe { (state, value) ->
                if (state !is DeviceConnectionFragment.State.Connected) {
                    return@subscribe
                }
                Log.d(tag, "Sending power to bluetooth $value")
                state.connection.sendValue(
                    value
                )
            }

    }
}
