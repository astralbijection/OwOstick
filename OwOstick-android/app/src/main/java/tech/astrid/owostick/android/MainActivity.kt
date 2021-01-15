package tech.astrid.owostick.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val tag = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


/*
        powerSlider.valueSubject
            .subscribe {
                when (val state = state.value) {
                    is DeviceConnectionFragment.ConnectingState.Connected -> state.connection.sendValue(it)
                }
            }*/
    }
}
