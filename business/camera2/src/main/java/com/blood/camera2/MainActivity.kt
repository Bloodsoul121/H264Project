package com.blood.camera2

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.TextureView
import com.blood.camera2.databinding.ActivityMainBinding
import com.blood.common.base.BasePermissionActivity
import com.blood.common.util.FileUtil
import com.blood.common.util.H264Util
import com.blood.common.util.YuvUtil
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

class MainActivity : BasePermissionActivity(), TextureView.SurfaceTextureListener, Camera2Listener {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private var camera2Helper: Camera2Helper? = null
    private var mediaCodec: MediaCodec? = null
    private var frameIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
        binding.textureView.surfaceTextureListener = this
        FileUtil.deleteFile(File(filesDir, "camera2.h264"))
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.i(TAG, "onSurfaceTextureAvailable: $width $height")
        camera2Helper = Camera2Helper(this, this).apply { start(surface) }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        Log.i(TAG, "onSurfaceTextureSizeChanged: $width $height")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Log.i(TAG, "onSurfaceTextureDestroyed: ")
        camera2Helper?.release()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        Log.i(TAG, "onSurfaceTextureUpdated: ")
    }

    private var nv21 = ByteArray(0) // 原相机合成格式 yuv -> nv21
    private var nv21_rotated = ByteArray(0)// 将数据旋转90度
    private var nv12 = ByteArray(0)// 输出数据格式

    override fun onPreview(y: ByteArray, u: ByteArray, v: ByteArray, previewSize: Size, stride: Int) {
        if (nv21.isEmpty()) {
            nv21 = ByteArray(stride * previewSize.height * 3 / 2)
            nv21_rotated = ByteArray(stride * previewSize.height * 3 / 2)
        }
        // 先转成nv21 , n21 横着 -> 竖着 , 再转成 yuv420
        YuvUtil.yuvToNv21(y, u, v, nv21, stride, previewSize.height)
        // 对数据进行旋转 90度
        YuvUtil.nv21_rotate_to_90(nv21, nv21_rotated, stride, previewSize.height)
        // Nv12  yuv420
        nv12 = YuvUtil.nv21toNV12(nv21_rotated)

        initMediaCodec(previewSize)
        startCodec()
    }

    private fun initMediaCodec(size: Size) {
        if (mediaCodec != null) {
            return
        }
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            // 注意这里的宽高，跟相机是反的
            val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, size.height, size.width)
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15) //15*2 =30帧
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 4000000)
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2) //2s一个I帧
            mediaCodec?.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun startCodec() {
        //输出成H264的码流
        val info = MediaCodec.BufferInfo()
        val inIndex: Int = mediaCodec?.dequeueInputBuffer(100000) ?: -1
        if (inIndex >= 0) {
            val byteBuffer: ByteBuffer = mediaCodec?.getInputBuffer(inIndex) ?: return
            byteBuffer.clear()
            byteBuffer.put(nv12, 0, nv12.size)
            val presentationTimeUs = computePresentationTime(frameIndex.toLong())
            mediaCodec?.queueInputBuffer(inIndex, 0, nv12.size, presentationTimeUs, 0)
            frameIndex++
        }
        val outIndex: Int = mediaCodec?.dequeueOutputBuffer(info, 100000) ?: -1
        if (outIndex >= 0) {
            val byteBuffer: ByteBuffer = mediaCodec?.getOutputBuffer(outIndex) ?: return
            val data = ByteArray(byteBuffer.remaining())
            byteBuffer[data]
            H264Util.writeBytes(data, File(filesDir, "camera2.h264"))
            mediaCodec?.releaseOutputBuffer(outIndex, false)
        }
    }

    private fun computePresentationTime(frameIndex: Long): Long {
        // 根据帧率计算出每一帧的大概时间点，即 1s -> 1000_000 / 15 是每一帧的时间间隔
        // 但是，第一帧不是从0开始的，因为dsp需要初始化时间，所以需要加上一个起始时间，随便多少，否则第一帧就直接跳过了
        return 132 + frameIndex * 1000000 / 15
    }

}