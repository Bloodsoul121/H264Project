package com.blood.touping.push

import android.media.projection.MediaProjection
import android.util.Log
import com.blood.common.util.H264Util
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
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
        pushLiveCodec = PushLiveCodec(this)
        pushLiveCodec?.startLive(mediaProjection)
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
            H264Util.writeContent(bytes, null)
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
            Log.i(TAG, "onClose: thread " + Thread.currentThread().name)
        }

        override fun onMessage(conn: WebSocket, message: String) {
            Log.i(TAG, "onMessage: ")
        }

        override fun onError(conn: WebSocket, ex: Exception) {
            Log.i(TAG, "onError: thread " + Thread.currentThread().name)
        }

        override fun onStart() {
            Log.i(TAG, "onStart: thread " + Thread.currentThread().name)
        }
    }

}