package com.blood.x264.channel

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.blood.common.util.YuvUtil
import com.blood.x264.push.LivePusher

class VideoChannel(
        private val livePusher: LivePusher,
        private val fps: Int,
        private val bitrate: Int
) {

    companion object {
        private const val TAG = "VideoChannel"
    }

    private var isRunning = false
    private var isFirstInit = true

    private var nv21: ByteArray = ByteArray(0)
    private var nv21_rotated: ByteArray = ByteArray(0)
    private var nv12: ByteArray = ByteArray(0)

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    init {
        handlerThread = HandlerThread("VideoChannel").apply { start() }
        handler = Handler(handlerThread!!.looper)
    }

    fun startLive() {
        isRunning = true
    }

    fun stopLive() {
        isRunning = false
    }

    // 相机源数据
    fun onSizeChanged(width: Int, height: Int) {
        livePusher.nativeInitVideoCodec(height, width, fps, bitrate)
    }

    fun analyzeFrameData(y: ByteArray, u: ByteArray, v: ByteArray, width: Int, height: Int, rowStride: Int) {
        handler?.post { analyze(y, u, v, width, height, rowStride) }
    }

    private fun analyze(y: ByteArray, u: ByteArray, v: ByteArray, width: Int, height: Int, rowStride: Int) {
        if (isRunning) {
            if (isFirstInit) {
                isFirstInit = false
                // 配置真实宽高，以最终传到编码器的为准
                livePusher.nativeInitVideoCodec(height, width, fps, bitrate)
                Log.i(TAG, "analyze: $height $width")
            }
            if (nv21.isEmpty()) {
                Log.i(TAG, "analyze: init nv21")
                nv21 = ByteArray(rowStride * height * 3 / 2)
                nv21_rotated = ByteArray(rowStride * height * 3 / 2)
            }
            YuvUtil.yuvToNv21(y, u, v, nv21, rowStride, height)
            YuvUtil.nv21_rotate_to_90(nv21, nv21_rotated, rowStride, height)
            nv12 = YuvUtil.nv21toNV12(nv21_rotated)
            livePusher.nativeSendVideoData(nv12)
        }
    }
}