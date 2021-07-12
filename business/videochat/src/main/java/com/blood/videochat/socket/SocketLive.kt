package com.blood.videochat.socket

interface SocketLive {
    fun connect(serverPort: Int)
    fun sendData(bytes: ByteArray)
    fun close()
}