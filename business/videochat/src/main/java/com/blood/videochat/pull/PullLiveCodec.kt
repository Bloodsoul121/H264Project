package com.blood.videochat.pull

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import com.blood.common.helper.SurfaceViewHelper
import com.blood.common.util.MediaCodecUtil
import com.blood.videochat.socket.SocketCallback
import com.blood.videochat.socket.SocketLive
import java.io.IOException
import java.nio.ByteBuffer

class PullLiveCodec : SocketCallback, SurfaceViewHelper.Callback {

    private var surface: Surface? = null
    private var pullSocketLive: SocketLive? = null
    private var mediaCodec: MediaCodec? = null
    private var isRunning = false
    private var isStart = false
    private var isNeedResume = false
    private var width = 0
    private var height = 0

    fun startLive(pullSocketLive: SocketLive) {
        this.pullSocketLive = pullSocketLive
        isRunning = true
        isStart = true
        mediaCodec?.start()
    }

    fun stopLive() {
        isRunning = false
        isStart = false
        MediaCodecUtil.releaseMediaCodec(mediaCodec)
        mediaCodec = null
    }

    override fun onSize(width: Int, height: Int) {
        this.width = width
        this.height = height
        if (mediaCodec != null) return
        try {
            MediaCodecUtil.releaseMediaCodec(mediaCodec)
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            mediaCodec?.configure(mediaFormat, surface, null, 0)
            if (isStart) mediaCodec?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onSurfaceCreated(surface: Surface) {
        this.surface = surface
        if (isNeedResume) {
            isNeedResume = false
            onSize(width, height)
        }
    }

    override fun onSurfaceDestroyed() {
        if (isRunning) {
            isNeedResume = true
            MediaCodecUtil.releaseMediaCodec(mediaCodec)
            mediaCodec = null
        }
    }

    override fun onReceive(data: ByteBuffer) {
        val byteArray = ByteArray(data.remaining())
        data.get(byteArray)

        mediaCodec?.let {
            val index: Int = mediaCodec!!.dequeueInputBuffer(100000)
            if (index >= 0) {
                val inputBuffer: ByteBuffer = mediaCodec!!.getInputBuffer(index)!!
                inputBuffer.clear()
                inputBuffer.put(byteArray, 0, byteArray.size)
                mediaCodec!!.queueInputBuffer(index, 0, byteArray.size, System.currentTimeMillis(), 0)
            }

            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 100000)
            while (outputBufferIndex >= 0) {
                mediaCodec!!.releaseOutputBuffer(outputBufferIndex, true)
                outputBufferIndex = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 0)
            }
        }
    }

}