package com.blood.common.util

import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.projection.MediaProjection

class MediaCodecUtil {

    companion object {

        fun getOutputBufferBytes(mediaCodec: MediaCodec, outputBufferIndex: Int, bufferInfo: MediaCodec.BufferInfo): ByteArray {
            val outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
            outputBuffer!!.position(bufferInfo.offset)
            outputBuffer.limit(bufferInfo.size)
            val byteArray = ByteArray(outputBuffer.remaining())
            outputBuffer[byteArray]
            outputBuffer.clear()
            return byteArray
        }

        fun releaseMediaCodec(mediaCodec: MediaCodec?) {
            mediaCodec?.stop()
            mediaCodec?.release()
        }

        fun releaseMediaProjection(mediaProjection: MediaProjection?) {
            mediaProjection?.stop()
        }

        fun releaseVirtualDisplay(virtualDisplay: VirtualDisplay?) {
            virtualDisplay?.release()
        }

    }

}