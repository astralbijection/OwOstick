package tech.astrid.owostick.android

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import java.io.Closeable

interface DeviceConnector {
    val name: String
    fun connect(): Deferred<DeviceConnection>
}

interface DeviceConnection : Closeable {
    val name: String
    fun sendValue(value: Float): Job
}