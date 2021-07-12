package com.blood.touping.push

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.blood.common.Constants
import com.blood.common.util.MediaProjectionUtil
import com.blood.touping.MainActivity.Companion.SOCKET_PORT
import com.blood.touping.databinding.ActivityPushBinding

class PushActivity : AppCompatActivity() {

    companion object {
        const val TAG = "PushActivity"
    }

    private lateinit var binding: ActivityPushBinding
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var width = 0
    private var height = 0
    private var pushSocketLive: PushSocketLive? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPushBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.root.post {
            width = binding.root.width
            height = binding.root.height
            if (width % 2 == 1) width--
            if (height % 2 == 1) height--
            Log.i(TAG, "init: $width $height")
            mediaProjectionManager = MediaProjectionUtil.requestMediaProject(this, Constants.MEDIA_PROJECTION_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.MEDIA_PROJECTION_REQUEST_CODE) {
                val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
                pushSocketLive = PushSocketLive(SOCKET_PORT).apply { start(mediaProjection) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pushSocketLive?.stop()
    }

}