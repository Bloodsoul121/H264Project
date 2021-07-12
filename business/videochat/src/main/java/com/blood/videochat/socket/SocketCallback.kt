package com.blood.videochat.socket

import java.nio.ByteBuffer

interface SocketCallback {
    fun onSize(width: Int, height: Int)
    fun onReceive(data: ByteBuffer)
}