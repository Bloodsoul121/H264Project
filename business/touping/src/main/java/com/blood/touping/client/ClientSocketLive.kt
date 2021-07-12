package com.blood.touping.client

import android.util.Log
import com.blankj.utilcode.util.NetworkUtils
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer

class ClientSocketLive(private val callback: SocketCallback) {

    companion object {
        const val TAG = "ServerSocketLive"
    }

    private var clientSocket: ClientWebSocketClient? = null

    fun start(serverPort: Int) {
        try {
            val ip = NetworkUtils.getIPAddress(true)
            val uri = URI("ws://$ip:$serverPort")
            clientSocket = ClientWebSocketClient(callback, uri).apply { connect() }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        clientSocket?.close()
    }

    interface SocketCallback {
        fun callBack(data: ByteArray)
    }

    private class ClientWebSocketClient(private val callback: SocketCallback, serverUri: URI) : WebSocketClient(serverUri) {

        override fun onOpen(handshakedata: ServerHandshake) {
            Log.i(TAG, "onOpen: thread " + Thread.currentThread().name)
        }

        override fun onMessage(message: String) {

        }

        override fun onMessage(bytes: ByteBuffer) {
            val buf = ByteArray(bytes.remaining())
            bytes[buf]
            callback.callBack(buf)
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            Log.i(TAG, "onClose: $code $reason $remote")
        }

        override fun onError(ex: Exception) {
            Log.i(TAG, "onError: $ex")
        }
    }

}