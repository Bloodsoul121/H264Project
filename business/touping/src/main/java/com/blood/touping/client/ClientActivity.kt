package com.blood.touping.client

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.blood.common.util.MediaCodecUtil
import com.blood.touping.MainActivity.Companion.SOCKET_PORT
import com.blood.touping.databinding.ActivityServerBinding
import java.io.IOException
import java.nio.ByteBuffer

class ClientActivity : AppCompatActivity(), SurfaceHolder.Callback, ClientSocketLive.SocketCallback {

    private lateinit var binding: ActivityServerBinding
    private var serverSocketLive: ClientSocketLive? = null
    private var mediaCodec: MediaCodec? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.surfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startCodec(holder.surface)
        connect()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        serverSocketLive?.stop()
        MediaCodecUtil.releaseMediaCodec(mediaCodec)
        mediaCodec = null
    }

    private fun startCodec(surface: Surface) {
        val width = 720
        val height = 1280
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            mediaCodec?.configure(mediaFormat, surface, null, 0)
            mediaCodec?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun connect() {
        serverSocketLive = ClientSocketLive(this).apply { start(SOCKET_PORT) }
    }

    override fun callBack(data: ByteArray) {
        mediaCodec?.let {
            val index: Int = mediaCodec!!.dequeueInputBuffer(100000)
            if (index >= 0) {
                val inputBuffer: ByteBuffer = mediaCodec!!.getInputBuffer(index)!!
                inputBuffer.clear()
                inputBuffer.put(data, 0, data.size)
                mediaCodec!!.queueInputBuffer(index, 0, data.size, System.currentTimeMillis(), 0)
            }

            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 100000)

            while (outputBufferIndex > 0) {
                mediaCodec!!.releaseOutputBuffer(outputBufferIndex, true)
                outputBufferIndex = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 0)
            }
        }
    }

}