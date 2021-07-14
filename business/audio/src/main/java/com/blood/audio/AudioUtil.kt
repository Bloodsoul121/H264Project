package com.blood.audio

import android.media.*
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.max

/**
 * 暂时只适合 44100 2通道，其他的要转码，也就是使用 MediaMuxer 重新采样
 */
class AudioUtil {

    companion object {

        private const val TAG = "AudioUtil"

        // 剪辑音频，合成新音频
        fun clipAudio(srcPath: String, dstPath: String, pcmPath: String, startTimeUs: Long, endTimeUs: Long): Boolean {
            try {
                // 将源音频文件解码为 pcm 文件
                val mediaFormat = decode2Pcm(srcPath, pcmPath, startTimeUs, endTimeUs)
                        ?: return false
                val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                val channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                Log.i(TAG, "clipAudio: $sampleRate $channelCount")
                // pcm 合成 wav 文件，没有压缩的，只是加了一个 wav 头信息
                PcmToWavUtil(sampleRate, AudioFormat.CHANNEL_IN_STEREO, channelCount, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(pcmPath, dstPath)
                Log.i(TAG, "clipAudio: over")
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            return true
        }

        fun decode2Pcm(audioPath: String, pcmPath: String, startTimeUs: Long, endTimeUs: Long): MediaFormat? {
            val channel = FileOutputStream(File(pcmPath)).channel

            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(audioPath)

            val trackIndex: Int = selectTrack(mediaExtractor, true)
            if (trackIndex < 0) {
                return null
            }

            mediaExtractor.selectTrack(trackIndex)
            mediaExtractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            val trackFormat = mediaExtractor.getTrackFormat(trackIndex)

            val maxInputSize = if (trackFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else {
                100000
            }
            val byteBuffer = ByteBuffer.allocateDirect(maxInputSize)

            val mediaCodec = MediaCodec.createDecoderByType(trackFormat.getString(MediaFormat.KEY_MIME)!!)
            mediaCodec.configure(trackFormat, null, null, 0)
            mediaCodec.start()

            val info = MediaCodec.BufferInfo()
            loop@ while (true) {
                val index = mediaCodec.dequeueInputBuffer(10000)
                if (index > -1) {

                    // 采样时间，判断区间
                    val sampleTime = mediaExtractor.sampleTime
                    when {
                        sampleTime == -1L -> break@loop
                        sampleTime > endTimeUs -> break@loop
                        sampleTime < startTimeUs -> {
                            mediaExtractor.advance()
                            continue@loop
                        }
                    }

                    info.size = mediaExtractor.readSampleData(byteBuffer, 0) // 读取采样数据
                    info.presentationTimeUs = mediaExtractor.sampleTime // 采样时间
                    info.flags = mediaExtractor.sampleFlags // 采样标志位

                    val content = ByteArray(byteBuffer.remaining())
                    byteBuffer.get(content) // 拿到数据

                    // 开始 mediaCodec 解码
                    val inputBuffer = mediaCodec.getInputBuffer(index) ?: continue@loop
                    inputBuffer.put(content)
                    mediaCodec.queueInputBuffer(index, 0, info.size, info.presentationTimeUs, info.flags)

                    mediaExtractor.advance() // 跳到下一帧
                }

                var outIndex = mediaCodec.dequeueOutputBuffer(info, 10000)
                while (outIndex > -1) {
                    val outputBuffer = mediaCodec.getOutputBuffer(outIndex)
                    channel.write(outputBuffer)
                    mediaCodec.releaseOutputBuffer(outIndex, false)
                    outIndex = mediaCodec.dequeueOutputBuffer(info, 10000)
                }
            }

            channel.close()
            mediaExtractor.release()
            mediaCodec.stop()
            mediaCodec.release()

            Log.i(TAG, "decode2Pcm: over")

            return trackFormat
        }

        private fun selectTrack(mediaExtractor: MediaExtractor, isAudio: Boolean): Int {
            val numTracks = mediaExtractor.trackCount // 获取所有轨道
            for (i in 0 until numTracks) {
                val format = mediaExtractor.getTrackFormat(i) // 获取轨道信息
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (isAudio) {
                    if (mime!!.startsWith("audio/")) {
                        return i
                    }
                } else {
                    if (mime!!.startsWith("video/")) {
                        return i
                    }
                }
            }
            return -1
        }

        // 混合多个音频
        fun mixAudio(videoInput: String,  // 视频音频
                     audioInput: String,  // bgm音频
                     output: String,  // 输出音频
                     videoInputTemp: String,  // 视频音频，pcm临时文件
                     audioInputTemp: String,  // bgm音频，pcm临时文件
                     mixTemp: String,  // 混合音频，pcm临时文件
                     startTimeUs: Long, endTimeUs: Long,
                     videoVolume: Int,  // 视频声音大小
                     aacVolume: Int // 音频声音大小
        ) {
            try {

                // 解码 pcm
                decode2Pcm(videoInput, videoInputTemp, startTimeUs, endTimeUs)
                decode2Pcm(audioInput, audioInputTemp, startTimeUs, endTimeUs)

                // 混音 pcm
                mixPcm(videoInputTemp, audioInputTemp, mixTemp, videoVolume, aacVolume)

                // pcm 合成 wav
                PcmToWavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, 2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(mixTemp, output)

                Log.i(TAG, "mixAudio: over")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // vol1  vol2  0-100  0静音  120
        fun mixPcm(pcm1Path: String, pcm2Path: String, toPath: String, volume1: Int, volume2: Int) {

            // 精准化音量值
            val vol1: Float = normalizeVolume(volume1)
            val vol2: Float = normalizeVolume(volume2)

            // 一次读取多一点 2kb
            val buffer1 = ByteArray(2048)
            val buffer2 = ByteArray(2048)
            // 待输出数据
            val buffer3 = ByteArray(2048)
            val fis1 = FileInputStream(pcm1Path)
            val fis2 = FileInputStream(pcm2Path)
            val fos = FileOutputStream(toPath)
            var temp1: Short
            var temp2: Short
            var temp: Int // 两个short变量相加 会大于short 声音 采样值 两个字节 65535 (-32768 - 32767)
            var end1 = false
            var end2 = false
            while (!end1 || !end2) {
                if (!end1) {
                    end1 = fis1.read(buffer1) == -1

                    // 拷贝到buffer3，先拷贝进来，万一 fis2 的数据不够呢
                    System.arraycopy(buffer1, 0, buffer3, 0, buffer1.size)
                }
                if (!end2) {
                    end2 = fis2.read(buffer2) == -1

                    // 一个声音为两个字节，所以 +2，将两个音频源进行合并
                    // 声音格式，低8位 + 高8位
                    var i = 0
                    while (i < buffer2.size) {
                        temp1 = ((buffer1[i].toInt() and 0xff) or ((buffer1[i + 1].toInt() and 0xff) shl 8)).toShort()
                        temp2 = ((buffer2[i].toInt() and 0xff) or ((buffer2[i + 1].toInt() and 0xff) shl 8)).toShort()

                        // 合并音乐和视频声音，直接两个short值相加，然后再分割为两个byte
                        temp = (temp1 * vol1 + temp2 * vol2).toInt()

                        // 考虑越界的问题
                        if (temp > 32767) {
                            temp = 32767
                        } else if (temp < -32768) {
                            temp = -32768
                        }
                        buffer3[i] = (temp and 0xFF).toByte() // 低8位
                        buffer3[i + 1] = ((temp ushr 8) and 0xFF).toByte() // 高8位，无符号位移
                        i += 2
                    }
                    fos.write(buffer3)
                }
            }
            fis1.close()
            fis2.close()
            fos.close()
            Log.i(TAG, "mixPcm: over")
        }

        private fun normalizeVolume(volume: Int): Float {
            return volume / 100f * 1 // 浮点计算，保持精准度
        }

        // 视频 + BGM ，封装新的视频mp4
        fun mixVideoAudio2Mp4(videoInput: String,  // 视频音频
                              audioInput: String,  // bgm音频
                              outputMp3: String,  // 输出音频
                              outputMp4: String,  // 输出音频
                              videoInputTemp: String,  // 视频音频，pcm临时文件
                              audioInputTemp: String,  // bgm音频，pcm临时文件
                              mixTemp: String,  // 混合音频，pcm临时文件
                              startTimeUs: Long, endTimeUs: Long,  // 时间段
                              videoVolume: Int,  // 视频声音大小
                              aacVolume: Int // 音频声音大小
        ) {
            try {

//                // 解码 pcm
//                decode2Pcm(videoInput, videoInputTemp, startTimeUs, endTimeUs)
//                decode2Pcm(audioInput, audioInputTemp, startTimeUs, endTimeUs)
//
//                // 混音 pcm
//                mixPcm(videoInputTemp, audioInputTemp, mixTemp, videoVolume, aacVolume)
//
//                // pcm 合成 wav
//                PcmToWavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, 2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(mixTemp, outputMp3)

                mixAudio(videoInput, audioInput, outputMp3, videoInputTemp, audioInputTemp, mixTemp, startTimeUs, endTimeUs, videoVolume, aacVolume)

                // 将源视频的视频通道与新合成的音频通道合并
                mixVideoAudio2Mp4(videoInput, outputMp3, outputMp4, startTimeUs, endTimeUs)

                Log.i(TAG, "mixVideoAudio2Mp4: over")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        // 抽离视频，与单独音频混合，封装新的视频mp4
        private fun mixVideoAudio2Mp4(mp4Input: String,  // 视频文件（包含视频和音频）
                                      wavInput: String,  // 单独音频，wav文件
                                      output: String,  // 输出视频，mp4
                                      startTimeUs: Long, endTimeUs: Long // 时间段
        ) {
            try {
                // 封装容器
                val mediaMuxer = MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

                // 抽离视频
                val mp4Extractor = MediaExtractor()
                mp4Extractor.setDataSource(mp4Input)
                // 视频轨道索引
                val videoTrackIndex = selectTrack(mp4Extractor, false)
                // 音频轨道索引
                val audioTrackIndex = selectTrack(mp4Extractor, true)

                // 往容器添加轨道信息
                val videoTrackFormat = mp4Extractor.getTrackFormat(videoTrackIndex)
                // 空轨道，没有数据的
                mediaMuxer.addTrack(videoTrackFormat)
                val audioTrackFormat = mp4Extractor.getTrackFormat(audioTrackIndex)
                // 配置信息，后面添加新的音频时需要用到
                val audioBitRate = audioTrackFormat.getInteger(MediaFormat.KEY_BIT_RATE)
                // 后面需要添加aac音频
                audioTrackFormat.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC)
                val muxerAudioIndex = mediaMuxer.addTrack(audioTrackFormat)

                // 配置完成，开始运行工作
                mediaMuxer.start()

                // 正片开始啦！！！

                // 将单独音频数据添加到轨道中
                val wavExtractor = MediaExtractor()
                wavExtractor.setDataSource(wavInput)
                val wavTrackIndex = selectTrack(wavExtractor, true)
                wavExtractor.selectTrack(wavTrackIndex)
                val wavMediaFormat = wavExtractor.getTrackFormat(wavTrackIndex)
                var maxBufferSize = if (wavMediaFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    wavMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                } else {
                    100000
                }
                val mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
                // 参数对应-> mime type、采样率、声道数
                val mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2)
                // 比特率
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, audioBitRate)
                // 音质等级
                mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                // 解码  那段
                mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxBufferSize)
                mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                mediaCodec.start()
                var byteBuffer = ByteBuffer.allocateDirect(maxBufferSize)
                val bufferInfo = MediaCodec.BufferInfo()
                var encodeDone = false
                while (!encodeDone) {
                    val inputBufferIndex = mediaCodec.dequeueInputBuffer(10000)
                    if (inputBufferIndex > -1) {
                        val sampleTime = wavExtractor.sampleTime // 裁剪文件默认从0开始了
                        if (sampleTime < 0) {
                            // pts小于0  来到了文件末尾 通知编码器  不用编码了
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        } else {
                            val flags = wavExtractor.sampleFlags
                            val size = wavExtractor.readSampleData(byteBuffer, 0)
                            val inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex)
                            inputBuffer!!.clear()
                            inputBuffer.put(byteBuffer)
                            inputBuffer.position(0)
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, size, sampleTime, flags)

                            // 下一帧
                            wavExtractor.advance()
                        }
                    }
                    var outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
                    while (outputBufferIndex > -1) {
                        if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            encodeDone = true
                            break
                        }
                        val outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
                        mediaMuxer.writeSampleData(muxerAudioIndex, outputBuffer!!, bufferInfo)
                        mediaCodec.releaseOutputBuffer(outputBufferIndex, false)

                        // 循环
                        outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
                    }
                }

                // 音频端就结束了
                if (audioTrackIndex > -1) {
                    mp4Extractor.unselectTrack(audioTrackIndex)
                }

                // 操作视频
                mp4Extractor.selectTrack(videoTrackIndex)
                mp4Extractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                maxBufferSize = videoTrackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                byteBuffer = ByteBuffer.allocateDirect(maxBufferSize)
                //封装容器添加视频轨道信息
                while (true) {
                    val sampleTimeUs = mp4Extractor.sampleTime
                    if (sampleTimeUs == -1L) {
                        break
                    }
                    if (sampleTimeUs < startTimeUs) {
                        mp4Extractor.advance()
                        continue
                    }
                    if (sampleTimeUs > endTimeUs) {
                        break
                    }
                    // pts  0
                    bufferInfo.presentationTimeUs = sampleTimeUs - startTimeUs + 600
                    bufferInfo.flags = mp4Extractor.sampleFlags
                    // 读取视频文件的数据  画面 数据   压缩1  未压缩2
                    bufferInfo.size = mp4Extractor.readSampleData(byteBuffer, 0)
                    if (bufferInfo.size < 0) {
                        break
                    }
                    // 视频轨道  画面写完了
                    mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo)
                    mp4Extractor.advance()
                }
                wavExtractor.release()
                mp4Extractor.release()
                mediaCodec.stop()
                mediaCodec.release()
                mediaMuxer.release()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun appendVideo(videoInput1: String,  // 视频1
                        videoInput2: String,  // 视频2
                        outputMp4: String // 输出合成视频
        ) {
            try {
                val mediaMuxer = MediaMuxer(outputMp4, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val mediaExtractor1 = MediaExtractor().apply { setDataSource(videoInput1) }
                val mediaExtractor2 = MediaExtractor().apply { setDataSource(videoInput2) }

                val videoTrackIndex1: Int = selectTrack(mediaExtractor1, false)
                val videoTrackFormat1 = mediaExtractor1.getTrackFormat(videoTrackIndex1)
                val videoDuration = videoTrackFormat1.getLong(MediaFormat.KEY_DURATION)
                val videoTrackIndex = mediaMuxer.addTrack(videoTrackFormat1)

                val audioTrackIndex1: Int = selectTrack(mediaExtractor1, true)
                val audioTrackFormat1 = mediaExtractor1.getTrackFormat(audioTrackIndex1)
                val audioDuration = audioTrackFormat1.getLong(MediaFormat.KEY_DURATION)
                val audioTrackIndex = mediaMuxer.addTrack(audioTrackFormat1)

                val fileDuration1 = max(videoDuration, audioDuration)

                val videoTrackIndex2: Int = selectTrack(mediaExtractor2, false)
                val audioTrackIndex2: Int = selectTrack(mediaExtractor2, true)

                mediaMuxer.start()

                var byteBuffer = ByteBuffer.allocateDirect(500 * 1024)
                var bufferInfo = MediaCodec.BufferInfo()

                mediaExtractor1.selectTrack(videoTrackIndex1)

                // 视频1 - 视频通道
                while (true) {
                    val size = mediaExtractor1.readSampleData(byteBuffer, 0)
                    if (size < 0) { // 表示读完了
                        break
                    }
                    bufferInfo.offset = 0
                    bufferInfo.presentationTimeUs = mediaExtractor1.sampleTime
                    bufferInfo.flags = mediaExtractor1.sampleFlags
                    bufferInfo.size = size
                    mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo)
                    mediaExtractor1.advance()
                }

                bufferInfo.presentationTimeUs = 0
                mediaExtractor1.unselectTrack(videoTrackIndex1)
                mediaExtractor1.selectTrack(audioTrackIndex1)

                // 视频1 - 音频通道
                while (true) {
                    val size = mediaExtractor1.readSampleData(byteBuffer, 0)
                    if (size < 0) {
                        break
                    }
                    bufferInfo.offset = 0
                    bufferInfo.presentationTimeUs = mediaExtractor1.sampleTime
                    bufferInfo.flags = mediaExtractor1.sampleFlags
                    bufferInfo.size = size
                    mediaMuxer.writeSampleData(audioTrackIndex, byteBuffer, bufferInfo)
                    mediaExtractor1.advance()
                }

                byteBuffer = ByteBuffer.allocateDirect(500 * 1024)
                bufferInfo = MediaCodec.BufferInfo()
                bufferInfo.presentationTimeUs = 0
                mediaExtractor2.selectTrack(videoTrackIndex2)

                // 视频2 - 视频通道
                while (true) {
                    val size = mediaExtractor2.readSampleData(byteBuffer, 0)
                    if (size < 0) {
                        break
                    }
                    bufferInfo.offset = 0
                    bufferInfo.presentationTimeUs = fileDuration1 + mediaExtractor2.sampleTime
                    bufferInfo.flags = mediaExtractor2.sampleFlags
                    bufferInfo.size = size
                    mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo)
                    mediaExtractor2.advance()
                }

                bufferInfo.presentationTimeUs = 0
                mediaExtractor2.unselectTrack(videoTrackIndex2)
                mediaExtractor2.selectTrack(audioTrackIndex2)

                // 视频2 - 音频通道
                while (true) {
                    val size = mediaExtractor2.readSampleData(byteBuffer, 0)
                    if (size < 0) {
                        break
                    }
                    bufferInfo.offset = 0
                    bufferInfo.presentationTimeUs = fileDuration1 + mediaExtractor2.sampleTime
                    bufferInfo.flags = mediaExtractor2.sampleFlags
                    bufferInfo.size = size
                    mediaMuxer.writeSampleData(audioTrackIndex, byteBuffer, bufferInfo)
                    mediaExtractor2.advance()
                }

                mediaExtractor1.release()
                mediaExtractor2.release()
                mediaMuxer.stop()
                mediaMuxer.release()
                Log.i(TAG, "appendVideo: over")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

    }
}