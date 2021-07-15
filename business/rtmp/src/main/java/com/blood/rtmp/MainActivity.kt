package com.blood.rtmp

import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import com.blood.common.Constants.MEDIA_PROJECTION_REQUEST_CODE
import com.blood.common.base.BasePermissionActivity
import com.blood.common.util.MediaProjectionUtil
import com.blood.rtmp.databinding.ActivityMainBinding
import com.blood.rtmp.push.LivePusher

class MainActivity : BasePermissionActivity() {

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
        mediaProjectionManager = MediaProjectionUtil.requestMediaProject(this, MEDIA_PROJECTION_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        if (resultCode == RESULT_OK) {
            if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
                val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
                push(mediaProjection)
            }
        }
    }

    private fun push(mediaProjection: MediaProjection) {
        livePusher = LivePusher().apply { startLive(mediaProjection) }
    }

}