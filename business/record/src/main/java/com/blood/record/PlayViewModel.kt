package com.blood.record

import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.blood.common.util.ThreadPoolUtil
import java.io.*

class PlayViewModel : ViewModel() {

    companion object {
        const val TAG = "PlayViewModel"
    }

    private var isPlaying = false

    fun playPcm(file: File) {
        ThreadPoolUtil.getInstance().start {
            try {
                isPlaying = true

                val bufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT)
                val audioTrack = AudioTrack(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM)
                audioTrack.play()

                val dis = DataInputStream(BufferedInputStream(FileInputStream(file)))
                val buffer = ByteArray(bufferSize)

                Log.i(TAG, "start play")

                while (isPlaying) {
                    var i = 0
                    while (dis.available() > 0 && i < bufferSize) {
                        buffer[i] = dis.readByte()
                        i++
                    }

                    audioTrack.write(buffer, 0, bufferSize)

                    Log.i(TAG, "playPcm")

                    if (i < bufferSize) {
                        audioTrack.stop()
                        audioTrack.release()
                        dis.close()
                        isPlaying = false
                        break
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        isPlaying = false
    }

}