package com.blood.rtmp.codec

import android.media.*
import com.blood.common.util.MediaCodecUtil
import com.blood.common.util.ThreadPoolUtil
import com.blood.rtmp.bean.RTMPPackage
import com.blood.rtmp.bean.RTMPPackage.Companion.RTMP_PACKET_TYPE_AUDIO_DATA
import com.blood.rtmp.bean.RTMPPackage.Companion.RTMP_PACKET_TYPE_AUDIO_HEAD
import com.blood.rtmp.push.LivePusher
import java.io.IOException
import java.nio.ByteBuffer

class AudioCodec(private val livePusher: LivePusher) {

    private var mediaCodec: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var minBufferSize = 0
    private var startTime: Long = 0
    private var isRunning = false

    fun startLive() {
        try {
            val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1)
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC) // 录音质量
            format.setInteger(MediaFormat.KEY_BIT_RATE, 64000) // 码率
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                // 录音工具类  采样位数 通道数 采样频率   固定了   设备没关系  录音 数据一样的
                // AudioFormat.CHANNEL_IN_MONO 单通道
                minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        ThreadPoolUtil.getInstance().start { startCodec() }
    }

    fun stopLive() {
        isRunning = false
    }

    private fun startCodec() {
        isRunning = true
        mediaCodec?.start()
        audioRecord?.startRecording()

        // 发送音频之前，空数据头
        livePusher.addPackage(RTMPPackage(byteArrayOf(0x12, 0x08), 0, RTMP_PACKET_TYPE_AUDIO_HEAD))

        val buffer = ByteArray(minBufferSize)
        val bufferInfo = MediaCodec.BufferInfo()

        while (isRunning) {

            val len: Int = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (len <= 0) continue

            var index: Int = mediaCodec?.dequeueInputBuffer(0) ?: -1
            if (index >= 0) {
                val inputBuffer: ByteBuffer = mediaCodec?.getInputBuffer(index) ?: continue
                inputBuffer.clear()
                inputBuffer.put(buffer, 0, len)
                //填充数据后再加入队列
                mediaCodec?.queueInputBuffer(index, 0, len, System.nanoTime() / 1000, 0)
            }

            index = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
            while (index >= 0 && isRunning) {

                if (startTime == 0L) {
                    startTime = bufferInfo.presentationTimeUs / 1000
                }

                val outData = MediaCodecUtil.getOutputBufferBytes(mediaCodec!!, index, bufferInfo)
                val tms: Long = bufferInfo.presentationTimeUs / 1000 - startTime //设置时间戳
                val rtmpPackage = RTMPPackage(outData, tms, RTMP_PACKET_TYPE_AUDIO_DATA)
                livePusher.addPackage(rtmpPackage)

                mediaCodec?.releaseOutputBuffer(index, false)

                index = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
            }
        }

        isRunning = false
        audioRecord?.stop()
        audioRecord?.release()
        MediaCodecUtil.releaseMediaCodec(mediaCodec)
    }

}