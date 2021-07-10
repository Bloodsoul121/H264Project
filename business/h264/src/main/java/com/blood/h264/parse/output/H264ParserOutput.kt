package com.blood.h264.parse.output

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.SystemClock
import android.util.Log
import com.blood.common.util.*
import java.io.IOException

class H264ParserOutput(val context: Context, val filePath: String, val width: Int, val height: Int,val callback: H264ParseOutputActivity.Callback) {

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
            mediaCodec.configure(mediaFormat, null, null, 0)
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
        var intervalCount = 0

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

                intervalCount++
                if (intervalCount % 10 == 0) {
                    // 数据源只能做一种处理，不能多处使用，否则MediaCodec.stop()会报错！！！
                    // 只能选择是渲染到屏幕，或者存储到本地图片，如果同时调用，MediaCodec.stop()会报错！！！
                    // 将一帧图片保存到本地
                    val byteArray = MediaCodecUtil.getOutputBufferBytes(mediaCodec, outputBufferIndex, bufferInfo)
                    val argbBytes = YuvUtil.transformYuvBytes2ArgbBytes(byteArray, width, height)
//                    val bitmap = BitmapUtil.compress(argbBytes, File(context.filesDir, "H264ParserOutput.png"))
                    val bitmap = BitmapFactory.decodeByteArray(argbBytes, 0, argbBytes.size)
                    callback.onBitmapCompressed(bitmap)
                }

                mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
            } else {
                Log.w(TAG, "dequeueOutputBuffer failed")
            }
        }

        stop()
    }

}