package tech.astrid.owostick.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        deviceFragment = DeviceConnectionFragment.newInstance()
        serverFragment = ServerConnectionFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .add(R.id.deviceFragmentContainer, deviceFragment)
            .add(R.id.serverFragmentContainer, serverFragment)
            .commit()

        val inputSource = serverFragment.state.switchMap {
            if (it is ServerConnectionFragment.State.Connected) {
                binding.powerSlider.valueSubject
            } else {
                serverFragment.power
            }
        }

        combineLatest(
            deviceFragment.state,
            inputSource
                .throttleLast(100L, TimeUnit.MILLISECONDS)
        )
            .filter { (state, _) -> state is DeviceConnectionFragment.State.Connected }
            .subscribe { (state, value) ->
                (state as DeviceConnectionFragment.State.Connected).connection.sendValue(
                    value
                )
            }
    }
}
