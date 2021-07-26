package com.blood.filter.record

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.opengl.EGLContext
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.blood.common.util.H264Util
import com.blood.common.util.MediaCodecUtil
import com.blood.filter.util.ToastUtil.toast
import java.io.File
import java.io.IOException

class H264MediaRecorder(context: Context, glContext: EGLContext, width: Int, height: Int) {

    private val mContext: Context = context.applicationContext
    private var mWidth: Int
    private var mHeight: Int
    private val mGlContext: EGLContext
    private var mMediaCodec: MediaCodec? = null
    private var mSurface: Surface? = null
    private var mHandler: Handler? = null
    private var mIsStart = false
    private var mEglEnv: EGLEnv? = null
    private val mFilterCodecTxt: File
    private val mFilterCodecH264: File

    companion object {
        private const val TAG = "H264MediaRecorder"
    }

    init {
        mWidth = width
        mHeight = height
        mGlContext = glContext
        mFilterCodecTxt = File(mContext.filesDir, "record.txt")
        mFilterCodecH264 = File(mContext.filesDir, "record.h264")
        checkFileExist(mFilterCodecTxt)
        checkFileExist(mFilterCodecH264)
    }

    private fun checkFileExist(file: File) {
        if (file.exists()) {
            val delete = file.delete()
            if (delete) {
                toast("删除本地文件：" + file.absolutePath)
            }
        }
    }

    fun onSizeChanged(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    fun start(speed: Float) {
        initMediaCodec()
        initOpenGlEnv()
    }

    private fun initMediaCodec() {
        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight)
            //颜色空间 从 surface当中获得
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            //码率
            format.setInteger(MediaFormat.KEY_BIT_RATE, 1500000)
            //帧率
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
            //关键帧间隔
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
            //创建编码器
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            //配置编码器
            mMediaCodec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            //输入数据     byte[]   gpu  类似 mediaprojection
            mSurface = mMediaCodec!!.createInputSurface()
            //开启编码
            mMediaCodec!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun initOpenGlEnv() {
        //創建OpenGL 的 環境
        val handlerThread = HandlerThread("codec-gl")
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
        mHandler!!.post {
            mEglEnv = EGLEnv(mContext, mGlContext, mSurface, mWidth, mHeight)
            mIsStart = true
        }
    }

    fun stop() {
        mIsStart = false
        mHandler!!.post {
            codec(true)
            mMediaCodec!!.stop()
            mMediaCodec!!.release()
            mMediaCodec = null
            mEglEnv!!.release()
            mEglEnv = null
            mSurface = null
            mHandler!!.looper.quitSafely()
            mHandler = null
        }
    }

    fun recordFrame(texture: Int, timestamp: Long) {
        if (!mIsStart) {
            return
        }
        mHandler!!.post {
            mEglEnv!!.draw(texture, timestamp)
            codec(false)
        }
    }

    // 从MediaCodec里面取出已经解码的数据
    private fun codec(endOfStream: Boolean) {
        val bufferInfo = MediaCodec.BufferInfo()
        val index = mMediaCodec!!.dequeueOutputBuffer(bufferInfo, 10000)
        if (index >= 0) {
            Log.i(TAG, "mediaFormat: ${mMediaCodec!!.getOutputFormat(index)}")
            val outData = MediaCodecUtil.getOutputBufferBytes(mMediaCodec!!, index, bufferInfo)
            H264Util.writeContent(outData, mFilterCodecTxt)
            H264Util.writeBytes(outData, mFilterCodecH264)
            mMediaCodec!!.releaseOutputBuffer(index, false)
        }
    }

}