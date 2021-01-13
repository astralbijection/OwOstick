package tech.astrid.owostick.android

import java.io.Closeable

interface DeviceConnector {
    val name: String
    val drawable: Int
    fun connect(): DeviceConnection
}

interface DeviceConnection : Closeable {
    val name: String
    fun sendValue(value: Float)
}