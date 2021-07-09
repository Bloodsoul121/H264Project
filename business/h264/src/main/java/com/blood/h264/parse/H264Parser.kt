package com.blood.h264.parse

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import com.blood.common.util.FileUtil
import com.blood.common.util.H264Util
import com.blood.common.util.ThreadPoolUtil
import java.io.IOException

class H264Parser(val filePath: String, val surface: Surface, val width: Int, val height: Int) {

    companion object {
        const val TAG = "H264Parser"
    }

    private lateinit var mediaCodec: MediaCodec
    private var isRunning = false

    // 宽高暂时固定，这里是直接解析h264码流，正常情况下是可以通过哥伦布编码或者MediaExtractor解析出文件宽高
    init {
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            mediaCodec.configure(mediaFormat, surface, null, 0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun start() {
        isRunning = true
        mediaCodec.start()
        ThreadPoolUtil.getInstance().start {
            try {
                decode()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        isRunning = false
        mediaCodec.stop()
        mediaCodec.release()
    }

    private fun decode() {
        // 直接暴力获取全部的码流，这里只是测试，小文件可以这样用
        val bytes = FileUtil.getBytes(filePath) ?: return

        var curFrameIndex = -1

        while (isRunning) {

            if (curFrameIndex >= bytes.size) {
                return
            }

            val inputBufferIndex = mediaCodec.dequeueInputBuffer(10000)
            if (inputBufferIndex > -1) {
                val inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()

                // 查找下一帧的位置
                val nextFrameIndex = H264Util.findNextFrame(bytes, curFrameIndex + 1)
                if (nextFrameIndex == -1) {
                    Log.w(TAG, "findByFrame failed : $nextFrameIndex")
                    break
                }
                curFrameIndex = if (curFrameIndex == -1) 0 else curFrameIndex

                Log.d(TAG, "curFrameIndex : $curFrameIndex , nextFrameIndex : $nextFrameIndex")

                inputBuffer?.put(bytes, curFrameIndex, nextFrameIndex - curFrameIndex)
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, nextFrameIndex - curFrameIndex, 0, 0)

                curFrameIndex = nextFrameIndex

            } else {
                continue
            }

            SystemClock.sleep(16)

            val bufferInfo = MediaCodec.BufferInfo()
            val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputBufferIndex > -1) {
                mediaCodec.releaseOutputBuffer(outputBufferIndex, true)
            } else {
                Log.w(TAG, "dequeueOutputBuffer failed")
            }
        }

        stop()
    }

}