package com.blood.rtmp.push

import android.media.projection.MediaProjection
import android.util.Log
import com.blood.rtmp.bean.RTMPPackage
import com.blood.rtmp.codec.VideoCodec
import java.util.concurrent.LinkedBlockingQueue

class LivePusher : Thread() {

    companion object {
        const val TAG = "LivePusher"
    }

    private val rtmpQueue = LinkedBlockingQueue<RTMPPackage>()
    private var mediaProjection: MediaProjection? = null
    private var videoCodec: VideoCodec? = null
    private var isRunning = false

    fun startLive(mediaProjection: MediaProjection) {
        this.mediaProjection = mediaProjection
        this.videoCodec = VideoCodec(this)
        start()
    }

    fun stopLive() {
        isRunning = false
        videoCodec?.stopLive()
        interrupt()
    }

    fun addPackage(rtmpPackage: RTMPPackage) {
        rtmpQueue.add(rtmpPackage)
    }

    override fun run() {
        super.run()

        videoCodec?.startLive(mediaProjection!!)

        try {

            isRunning = true

            while (isRunning || isInterrupted) {

                // 数据推流
                val rtmpPackage = rtmpQueue.take()

                Log.i(TAG, "run: $rtmpPackage")
            }

        } catch (e: InterruptedException) {
            interrupt()
        }
    }

}