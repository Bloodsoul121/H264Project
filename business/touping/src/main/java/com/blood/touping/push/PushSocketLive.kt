package com.blood.touping.push

import android.media.projection.MediaProjection
import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.json.JSONObject
import java.io.IOException
import java.net.InetSocketAddress

class PushSocketLive(serverPort: Int) {

    companion object {
        const val TAG = "PushSocketLive"
    }

    private val pushSocket: PushWebSocketServer = PushWebSocketServer(InetSocketAddress(serverPort))
    private var pushLiveCodec: PushLiveCodec? = null

    fun start(mediaProjection: MediaProjection) {
        pushSocket.start()
        pushLiveCodec = PushLiveCodec(this).apply { startLive(mediaProjection) }
    }

    fun stop() {
        pushLiveCodec?.stopLive()
        try {
            pushSocket.close()
            pushSocket.stop()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun sendData(bytes: ByteArray) {
        pushSocket.sendData(bytes)
    }

    private class PushWebSocketServer(address: InetSocketAddress) : WebSocketServer(address) {

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

            val size = JSONObject()
            size.put("width", 0)
            size.put("height", 0)
            mWebSocket?.send(size.toString())
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
            Log.i(TAG, "onClose: $code $reason $remote")
        }

        override fun onMessage(conn: WebSocket, message: String) {
            Log.i(TAG, "onMessage: $message")
        }

        override fun onError(conn: WebSocket, ex: Exception) {
            Log.i(TAG, "onError: $ex")
        }

        override fun onStart() {
            Log.i(TAG, "onStart: ")
        }
    }

}