package com.blood.audio

import android.os.Bundle
import com.blood.audio.databinding.ActivityMainBinding
import com.blood.common.base.BasePermissionActivity
import com.blood.common.util.AssetsUtil
import com.blood.common.util.AudioUtil
import com.blood.common.util.FileUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : BasePermissionActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
        AssetsUtil.copyAssets(this, "audio.mp3")
        AssetsUtil.copyAssets(this, "doufuhua.mp4")
        AssetsUtil.copyAssets(this, "hot.mp4")

        binding.clipAudio.setOnClickListener {
            GlobalScope.launch {
                val srcPath = File(filesDir, "audio.mp3").absolutePath
                val dstPath = File(filesDir, "audioClip.mp3").absolutePath
                val pcmPath = File(filesDir, "audioPcm.pcm").absolutePath
                FileUtil.deleteFile(dstPath)
                FileUtil.deleteFile(pcmPath)
                AudioUtil.clipAudio(srcPath, dstPath, pcmPath, 20_000_000, 25_000_000)
            }
        }

        binding.mixAudio.setOnClickListener {
            GlobalScope.launch {
                val audioPath = File(filesDir, "doufuhua.mp4").absolutePath
                val bgPath = File(filesDir, "audio.mp3").absolutePath
                val mixPath = File(filesDir, "mix.mp3").absolutePath
                val pcmPath1 = File(filesDir, "mix1.pcm").absolutePath
                val pcmPath2 = File(filesDir, "mix2.pcm").absolutePath
                val pcmPath3 = File(filesDir, "mix.pcm").absolutePath
                FileUtil.deleteFile(pcmPath1)
                FileUtil.deleteFile(pcmPath2)
                FileUtil.deleteFile(pcmPath3)
                FileUtil.deleteFile(mixPath)
                AudioUtil.mixAudio(audioPath, bgPath, mixPath, pcmPath1, pcmPath2, pcmPath3, 10_000_000, 18_000_000, 100, 100)
            }
        }

        binding.mixAudioVideo.setOnClickListener {
            GlobalScope.launch {
                val videoPath = File(filesDir, "doufuhua.mp4").absolutePath
                val bgPath = File(filesDir, "audio.mp3").absolutePath
                val mixMp3Path = File(filesDir, "mix.mp3").absolutePath
                val mixMp4Path = File(filesDir, "mix.mp4").absolutePath
                val pcmPath1 = File(filesDir, "mix1.pcm").absolutePath
                val pcmPath2 = File(filesDir, "mix2.pcm").absolutePath
                val pcmPath3 = File(filesDir, "mix.pcm").absolutePath
                FileUtil.deleteFile(pcmPath1)
                FileUtil.deleteFile(pcmPath2)
                FileUtil.deleteFile(pcmPath3)
                FileUtil.deleteFile(mixMp3Path)
                FileUtil.deleteFile(mixMp4Path)
                AudioUtil.mixVideoAudio2Mp4(videoPath, bgPath, mixMp3Path, mixMp4Path, pcmPath1, pcmPath2, pcmPath3, 10_000_000, 18_000_000, 100, 100)
            }
        }

        binding.appendAudioVideo.setOnClickListener {
            GlobalScope.launch {
                val videoInput1 = File(filesDir, "doufuhua.mp4").absolutePath
                val videoInput2 = File(filesDir, "hot.mp4").absolutePath
                val outputMp4 = File(filesDir, "append.mp4").absolutePath
                FileUtil.deleteFile(outputMp4)
                AudioUtil.appendVideo(videoInput1, videoInput2, outputMp4)
            }
        }

    }

}