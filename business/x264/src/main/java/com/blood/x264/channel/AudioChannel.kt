package com.blood.x264.channel

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.blood.common.util.ThreadPoolUtil
import com.blood.x264.push.LivePusher
import kotlin.math.max

class AudioChannel(private val livePusher: LivePusher, val sampleRate: Int, val channels: Int) {

    private var isRunning = false
    private var buffer: ByteArray? = null
    private var audioRecord: AudioRecord? = null

    fun startLive() {
        if (isRunning) {
            return
        }
        // 双通道应该传的值   一律用单通道
        val channelConfig = if (channels == 2) AudioFormat.CHANNEL_IN_STEREO else AudioFormat.CHANNEL_IN_MONO
        // 数据大小 是根据mediacodec来的数据 怎么不准确 minBufferSize 参考值 软编 肯定返回 硬编
        // 硬编   不可以    minBufferSize  -1
        var minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT)
        // 初始化faac软编  inputByteNum  最小容器
        val inputByteNum: Int = livePusher.nativeInitAudioCodec(sampleRate, channels)
        minBufferSize = max(minBufferSize, inputByteNum)
        buffer = ByteArray(minBufferSize)

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT, minBufferSize)

        ThreadPoolUtil.getInstance().start(this::startCodec)
    }

    private fun startCodec() {
        isRunning = true
        audioRecord?.startRecording()

        while (isRunning && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            val len: Int = audioRecord?.read(buffer!!, 0, buffer!!.size) ?: -1
            if (len > 0) {
                // 这里除以2很重要，因为底层是以32位进行读取的，双通道
                // 个人觉得应该是 位深是16bit，是2个字节，所以除以2，不清楚。。。
                livePusher.nativeSendAudioData(buffer!!, len / 2)
            }
        }

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    fun stopLive() {
        isRunning = false
    }
}