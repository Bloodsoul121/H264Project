package com.blood.rtmp.push

import android.media.projection.MediaProjection
import android.util.Log
import com.blood.rtmp.bean.RTMPPackage
import com.blood.rtmp.codec.AudioCodec
import com.blood.rtmp.codec.VideoCodec
import java.util.concurrent.LinkedBlockingQueue

class LivePusher : Thread() {

    companion object {
        const val TAG = "LivePusher"

        init {
            System.loadLibrary("native-lib")
        }
    }

    private val rtmpQueue = LinkedBlockingQueue<RTMPPackage>()
    private var mediaProjection: MediaProjection? = null
    private var videoCodec: VideoCodec? = null
    private var audioCodec: AudioCodec? = null
    private var url: String? = null
    private var isRunning = false

    fun startLive(mediaProjection: MediaProjection, url: String) {
        this.url = url
        this.mediaProjection = mediaProjection
        this.videoCodec = VideoCodec(this)
        this.audioCodec = AudioCodec(this)
        start()
    }

    fun stopLive() {
        isRunning = false
        videoCodec?.stopLive()
        audioCodec?.stopLive()
        interrupt()
    }

    fun addPackage(rtmpPackage: RTMPPackage) {
        rtmpQueue.add(rtmpPackage)
    }

    override fun run() {
        super.run()

        if (!connect(url ?: return)) {
            Log.e(TAG, "connect failed")
            return
        }

        videoCodec?.startLive(mediaProjection!!)
        audioCodec?.startLive()

        try {
            isRunning = true
            while (isRunning || isInterrupted) {
                // 数据推流
                val rtmpPackage = rtmpQueue.take()
                sendData(rtmpPackage!!.buffer!!, rtmpPackage.buffer!!.size, rtmpPackage.tms, rtmpPackage.type)
            }
        } catch (e: InterruptedException) {
            interrupt()
        }
    }

    private external fun connect(url: String): Boolean
    private external fun sendData(data: ByteArray, len: Int, tms: Long, type: Int): Boolean

}