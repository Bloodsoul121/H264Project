package com.blood.record

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.blood.common.base.BasePermissionActivity
import com.blood.common.util.AssetsUtil
import com.blood.common.util.FileUtil
import com.blood.record.databinding.ActivityMainBinding
import java.io.File

class MainActivity : BasePermissionActivity() {

    private lateinit var binding: ActivityMainBinding
    private var recordViewModel: RecordViewModel? = null
    private var playViewModel: PlayViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
        AssetsUtil.copyAssets(this, "mix.pcm")
        FileUtil.deleteFile(File(filesDir, "record.pcm"))
        FileUtil.deleteFile(File(filesDir, "record.mp3"))

        recordViewModel = ViewModelProvider(this)[RecordViewModel::class.java]
        playViewModel = ViewModelProvider(this)[PlayViewModel::class.java]

        binding.startRecord.setOnClickListener { recordViewModel?.startRecord() }
        binding.stopRecord.setOnClickListener { recordViewModel?.stopRecord() }
        binding.playPcm.setOnClickListener { playViewModel?.playPcm(File(filesDir, "record.pcm")) }
        binding.stopPlayPcm.setOnClickListener { playViewModel?.stop() }
    }

}