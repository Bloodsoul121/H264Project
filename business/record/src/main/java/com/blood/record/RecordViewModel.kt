package com.blood.record

import android.media.*
import android.util.Log
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.Utils
import com.blood.common.util.H264Util
import com.blood.common.util.MediaCodecUtil
import com.blood.common.util.ThreadPoolUtil
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

class RecordViewModel : ViewModel() {

    companion object {
        const val TAG = "RecordViewModel"
    }

//    private var mediaCodec: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var minBufferSize = 0
    private var startTime: Long = 0
    private var isRunning = false

    fun startRecord() {
        try {
//            val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2)
//            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC) // 录音质量
//            format.setInteger(MediaFormat.KEY_BIT_RATE, 64000) // 码率
//            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
//                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//                // 录音工具类  采样位数 通道数 采样频率   固定了   设备没关系  录音 数据一样的
//                // AudioFormat.CHANNEL_IN_MONO 单通道
                minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT)
                audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize)
//            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        ThreadPoolUtil.getInstance().start { startCodec() }
    }

    private fun startCodec() {
        isRunning = true
//        mediaCodec?.start()
        audioRecord?.startRecording()

        val buffer = ByteArray(minBufferSize)
        val bufferInfo = MediaCodec.BufferInfo()

        while (isRunning) {

            val len: Int = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (len <= 0) continue

            H264Util.writeBytes(buffer, File(Utils.getApp().filesDir, "record.pcm"))

            Log.i(TAG, "startCodec: $len")

//            var index: Int = mediaCodec?.dequeueInputBuffer(0) ?: -1
//            if (index >= 0) {
//                val inputBuffer: ByteBuffer = mediaCodec?.getInputBuffer(index) ?: continue
//                inputBuffer.clear()
//                inputBuffer.put(buffer, 0, len)
//                //填充数据后再加入队列
//                mediaCodec?.queueInputBuffer(index, 0, len, System.nanoTime() / 1000, 0)
//            }
//
//            index = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
//            while (index >= 0 && isRunning) {
//
//                if (startTime == 0L) {
//                    startTime = bufferInfo.presentationTimeUs / 1000
//                }
//
//                val outData = MediaCodecUtil.getOutputBufferBytes(mediaCodec!!, index, bufferInfo)
//
//                H264Util.writeBytes(outData, File(Utils.getApp().filesDir, "record.mp3"))
//
//                mediaCodec?.releaseOutputBuffer(index, false)
//
//                index = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
//            }
        }

        isRunning = false
        audioRecord?.stop()
        audioRecord?.release()
//        MediaCodecUtil.releaseMediaCodec(mediaCodec)
    }

    fun stopRecord() {
        isRunning = false
    }

}