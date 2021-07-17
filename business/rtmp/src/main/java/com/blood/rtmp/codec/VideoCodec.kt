package com.blood.rtmp.codec

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Bundle
import android.view.Surface
import com.blood.common.util.MediaCodecUtil
import com.blood.common.util.ThreadPoolUtil
import com.blood.rtmp.bean.RTMPPackage
import com.blood.rtmp.push.LivePusher
import java.io.IOException

class VideoCodec(private val livePusher: LivePusher) {

    private var mediaCodec: MediaCodec? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var isRunning = false
    private var startTime: Long = 0
    private var timeStamp: Long = 0

    fun startLive(mediaProjection: MediaProjection) {
        this.mediaProjection = mediaProjection
        try {
            // 一般推流的宽高是 720*1280，参考云文档
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 400000)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                val surface: Surface = createInputSurface()
                virtualDisplay = mediaProjection.createVirtualDisplay("screen-codec", 720, 1280, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null)
                ThreadPoolUtil.getInstance().start { startCodec() }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopLive() {
        isRunning = false
    }

    private fun startCodec() {
        isRunning = true
        mediaCodec?.start()
        val bufferInfo = MediaCodec.BufferInfo()

        while (isRunning) {
            if (System.currentTimeMillis() - timeStamp >= 2000) {
                // dsp 芯片触发I帧
                val params = Bundle().apply { putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0) }
                mediaCodec?.setParameters(params)
                timeStamp = System.currentTimeMillis()
            }
            val index: Int = mediaCodec?.dequeueOutputBuffer(bufferInfo, 100000) ?: -1
            if (index >= 0) {

                if (startTime == 0L) {
                    startTime = bufferInfo.presentationTimeUs / 1000 // 毫秒
                }

                val outData = MediaCodecUtil.getOutputBufferBytes(mediaCodec!!, index, bufferInfo)
                val type = RTMPPackage.RTMP_PACKET_TYPE_VIDEO
                val tms = bufferInfo.presentationTimeUs / 1000 - startTime
                livePusher.addPackage(RTMPPackage(outData, tms, type)) // 发包

                mediaCodec?.releaseOutputBuffer(index, false)
            }
        }

        isRunning = false
        MediaCodecUtil.releaseMediaCodec(mediaCodec)
        MediaCodecUtil.releaseMediaProjection(mediaProjection)
        MediaCodecUtil.releaseVirtualDisplay(virtualDisplay)
    }

}