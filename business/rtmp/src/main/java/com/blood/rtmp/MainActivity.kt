package com.blood.rtmp

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import com.blankj.utilcode.util.Utils
import com.blood.common.Constants.MEDIA_PROJECTION_REQUEST_CODE
import com.blood.common.base.BasePermissionActivity
import com.blood.common.util.FileUtil
import com.blood.common.util.MediaProjectionUtil
import com.blood.rtmp.databinding.ActivityMainBinding
import com.blood.rtmp.push.LivePusher
import java.io.File

class MainActivity : BasePermissionActivity() {

    companion object {
        const val URL = "rtmp://live-push.bilivideo.com/live-bvc/?streamname=live_312497503_45360587&key=d502577a2f405faecb48cd56f433d03f&schedule=rtmp&pflag=1"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var livePusher: LivePusher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        livePusher?.stopLive()
    }

    override fun process() {
        FileUtil.deleteFile(File(Utils.getApp().filesDir, "audio.pcm"))
        FileUtil.deleteFile(File(Utils.getApp().filesDir, "audio.mp3"))

        binding.startLive.setOnClickListener {
            mediaProjectionManager = MediaProjectionUtil.requestMediaProject(this, MEDIA_PROJECTION_REQUEST_CODE)
        }
        binding.stopLive.setOnClickListener {
            livePusher?.stopLive()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        if (resultCode == RESULT_OK) {
            if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
                val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
                livePusher = LivePusher().apply { startLive(mediaProjection, URL) }
            }
        }
    }

}