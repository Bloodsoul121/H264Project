package com.blood.videochat.push

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.blood.common.helper.Camera1Helper
import com.blood.common.util.H264Util
import com.blood.common.util.MediaCodecUtil
import com.blood.videochat.App
import com.blood.videochat.socket.SocketLive
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

class PushLiveCodec : Camera1Helper.Callback {

    companion object {
        const val NAL_I = 19
        const val NAL_VPS = 32
    }

    private var socketLive: SocketLive? = null
    private var mediaCodec: MediaCodec? = null
    private var vps_sps_pps_buf = ByteArray(0)
    private var isRunning = false
    private var isStart = false
    private var frameIndex = 0

    fun startLive(socketLive: SocketLive) {
        this.socketLive = socketLive
        isRunning = true
        isStart = true
        mediaCodec?.start()
    }

    fun stopLive() {
        isStart = false
        isRunning = false
        MediaCodecUtil.releaseMediaCodec(mediaCodec)
    }

    override fun onSize(width: Int, height: Int) {
        try {
            MediaCodecUtil.releaseMediaCodec(mediaCodec)
            val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            mediaCodec?.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            if (isStart) mediaCodec?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCaptureData(bytes: ByteArray, width: Int, height: Int) {
        if (!isRunning) {
            return
        }

        val index: Int = mediaCodec?.dequeueInputBuffer(100000) ?: -1
        if (index > -1) {
            val inputBuffer: ByteBuffer = mediaCodec?.getInputBuffer(index) ?: return
            inputBuffer.clear()
            inputBuffer.put(bytes, 0, bytes.size)
            val presentationTimeUs = computePresentationTime(frameIndex.toLong())
            mediaCodec?.queueInputBuffer(index, 0, bytes.size, presentationTimeUs, 0)
            frameIndex++
        }

        val bufferInfo = MediaCodec.BufferInfo()
        var outputBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 10000) ?: -1
        while (outputBufferIndex > -1) {
            val byteArray = MediaCodecUtil.getOutputBufferBytes(mediaCodec!!, outputBufferIndex, bufferInfo)
//            H264Util.writeContent(byteArray, null)
//            H264Util.writeBytes(byteArray, File(App.context.filesDir, "push.h265"))
            parseFrame(byteArray)
            mediaCodec?.releaseOutputBuffer(outputBufferIndex, false)
            outputBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 10000) ?: -1
        }
    }

    private fun computePresentationTime(frameIndex: Long): Long {
        // ?????????????????????????????????????????????????????? 1s -> 1000_000 / 15 ???????????????????????????
        // ???????????????????????????0??????????????????dsp???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        return 132 + frameIndex * 1000000 / 15
    }

    private fun parseFrame(bytes: ByteArray) {
        // ?????????
        var offset = 4
        if (bytes[2].toInt() == 0x01) {
            offset = 3
        }
        // nalu???
        val naluType = (bytes[offset].toInt() and 0x7e) shr 1
        when (naluType) {
            NAL_VPS -> {
                // ?????? vps sps pps ?????????????????????????????????????????????
                vps_sps_pps_buf = bytes
            }
            NAL_I -> {
                // ???????????????I???????????????
                val newBuf = ByteArray(vps_sps_pps_buf.size + bytes.size)
                System.arraycopy(vps_sps_pps_buf, 0, newBuf, 0, vps_sps_pps_buf.size)
                System.arraycopy(bytes, 0, newBuf, vps_sps_pps_buf.size, bytes.size)
                socketLive?.sendData(newBuf)
            }
            else -> {
                socketLive?.sendData(bytes)
            }
        }
    }

}