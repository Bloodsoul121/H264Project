package com.blood.touping.push

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.view.Surface
import com.blood.common.util.MediaCodecUtil
import com.blood.common.util.ThreadPoolUtil
import java.io.IOException

class PushLiveCodec(private val pushSocketLive: PushSocketLive) : Runnable {

    companion object {
        const val NAL_I = 19
        const val NAL_VPS = 32
    }

    private var mediaCodec: MediaCodec? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var isRunning = false
    private var vps_sps_pps_buf = ByteArray(0)

    fun startLive(mediaProjection: MediaProjection) {
        this.mediaProjection = mediaProjection
        val width = 720
        val height = 1280
        try {
            val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            mediaCodec?.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface: Surface = mediaCodec?.createInputSurface() ?: return
            virtualDisplay = mediaProjection.createVirtualDisplay("screen-live", width, height, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, inputSurface, null, null)
            isRunning = true
            ThreadPoolUtil.getInstance().start(this)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopLive() {
        isRunning = false
    }

    override fun run() {
        mediaCodec?.start()
        while (isRunning) {
            val bufferInfo = MediaCodec.BufferInfo()
            val outputBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 10000) ?: -1
            if (outputBufferIndex > -1) {
                val bytes = MediaCodecUtil.getOutputBufferBytes(mediaCodec!!, outputBufferIndex, bufferInfo)
                parseFrame(bytes)
                mediaCodec?.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
        MediaCodecUtil.releaseMediaCodec(mediaCodec)
        MediaCodecUtil.releaseMediaProjection(mediaProjection)
        MediaCodecUtil.releaseVirtualDisplay(virtualDisplay)
    }

    private fun parseFrame(bytes: ByteArray) {
        // 分隔符
        var offset = 4
        if (bytes[2].toInt() == 0x01) {
            offset = 3
        }
        // nalu头
        val naluType = (bytes[offset].toInt() and 0x7e) shr 1
        when (naluType) {
            NAL_VPS -> {
                vps_sps_pps_buf = bytes
            }
            NAL_I -> {
                val newBuf = ByteArray(vps_sps_pps_buf.size + bytes.size)
                System.arraycopy(vps_sps_pps_buf, 0, newBuf, 0, vps_sps_pps_buf.size)
                System.arraycopy(bytes, 0, newBuf, vps_sps_pps_buf.size, bytes.size)
                pushSocketLive.sendData(newBuf)
            }
            else -> {
                pushSocketLive.sendData(bytes)
            }
        }
    }

}