package com.blood.videochat.pull

import android.util.Log
import com.blankj.utilcode.util.NetworkUtils
import com.blood.videochat.socket.SocketCallback
import com.blood.videochat.socket.SocketLive
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer

class PullSocketLive(private val socketCallback: SocketCallback) : SocketLive {

    companion object {
        const val TAG = "PullSocketLive"
    }

    private var clientSocket: ClientWebSocketClient? = null

    override fun connect(serverPort: Int) {
        try {
            val ip = NetworkUtils.getIPAddress(true)
            val uri = URI("ws://$ip:$serverPort")
            clientSocket = ClientWebSocketClient(uri, socketCallback).apply { connect() }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    override fun sendData(bytes: ByteArray) {
        clientSocket?.send(bytes)
    }

    override fun close() {
        clientSocket?.close()
    }

    private class ClientWebSocketClient(serverUri: URI, val callback: SocketCallback) : WebSocketClient(serverUri) {

        override fun onOpen(handshakedata: ServerHandshake) {
            Log.i(TAG, "onOpen: thread " + Thread.currentThread().name)
        }

        override fun onMessage(message: String) {
            val jsonObject = JSONObject(message)
            val width = jsonObject.optInt("width")
            val height = jsonObject.optInt("height")
            callback.onSize(width, height)
        }

        override fun onMessage(bytes: ByteBuffer) {
            callback.onReceive(bytes)
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            Log.i(TAG, "onClose: $code $reason $remote")
        }

        override fun onError(ex: Exception) {
            Log.i(TAG, "onError: $ex")
        }
    }

}