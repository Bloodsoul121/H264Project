package com.blood.videochat.push

import android.util.Log
import com.blood.videochat.socket.SocketCallback
import com.blood.videochat.socket.SocketLive
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class PushSocketLive(private val socketCallback: SocketCallback) : SocketLive {

    companion object {
        const val TAG = "PushSocketLive"
    }

    private var pushSocket: PushWebSocketServer? = null

    override fun connect(serverPort: Int) {
        pushSocket = PushWebSocketServer(InetSocketAddress(serverPort), socketCallback).apply { start() }
    }

    override fun sendData(bytes: ByteArray) {
        pushSocket?.sendData(bytes)
    }

    override fun close() {
        try {
            pushSocket?.close()
            pushSocket?.stop()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private class PushWebSocketServer(address: InetSocketAddress, val socketCallback: SocketCallback) : WebSocketServer(address) {

        private var mWebSocket: WebSocket? = null

        fun sendData(bytes: ByteArray) {
            if (mWebSocket?.isOpen == true) {
                mWebSocket?.send(bytes)
            }
        }

        fun close() {
            mWebSocket?.close()
        }

        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            Log.i(TAG, "onOpen: thread " + Thread.currentThread().name)
            mWebSocket = conn
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
            Log.i(TAG, "onClose: $code $reason $remote")
        }

        override fun onMessage(conn: WebSocket, message: String) {
        }

        override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {
            message ?: return
            socketCallback.onReceive(message)
        }

        override fun onError(conn: WebSocket, ex: Exception) {
            Log.i(TAG, "onError: $ex")
        }

        override fun onStart() {
            Log.i(TAG, "onStart: ")
        }
    }

}