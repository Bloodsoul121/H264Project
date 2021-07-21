package com.blood.x264.push

import android.util.Log
import com.blood.x264.channel.AudioChannel
import com.blood.x264.channel.VideoChannel

class LivePusher {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }

        // jni回调java层的方法  byte[] data    char *data
        @JvmStatic
        private fun postData(data: ByteArray) {
            Log.i("LivePusher", "postData: " + data.size)
        }
    }

    val videoChannel = VideoChannel(this, 10, 800_000)
    val audioChannel = AudioChannel(this, 44100, 2)

    init {
        nativeInit()
    }

    fun startLive(path: String) {
        nativeStart(path)
        videoChannel.startLive()
        audioChannel.startLive()
    }

    fun stopLive() {
        videoChannel.stopLive()
        audioChannel.stopLive()
        nativeStop()
    }

    // 初始化
    external fun nativeInit()

    external fun nativeStart(path: String)

    // 绑定真实的宽高
    external fun nativeInitVideoCodec(width: Int, height: Int, fps: Int, bitrate: Int)

    external fun nativeSendVideoData(data: ByteArray)

    external fun nativeInitAudioCodec(sampleRate: Int, channels: Int): Int

    external fun nativeSendAudioData(buffer: ByteArray, len: Int)

    external fun nativeStop()

    external fun nativeRelease()

}