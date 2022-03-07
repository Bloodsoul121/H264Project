package com.blood.demo

import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.blood.common.base.RequestPermissionActivity
import com.blood.demo.databinding.ActivityMainBinding
import java.io.File
import kotlin.concurrent.thread

class MainActivity : RequestPermissionActivity(), SurfaceHolder.Callback {

    companion object {
        init {
            System.loadLibrary("ffmpeg-lib")
        }

        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var url: String

    override fun onCreateView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
        val saveDir = File(filesDir, "ffmpeg").absolutePath
        AssetsUtil.copyFileFromAssets(this, "hot.mp4", saveDir, "ffmpeg.mp4")
        url = File(saveDir, "ffmpeg.mp4").absolutePath

        binding.tv.text = testFfmpeg()
        binding.surfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.i(TAG, "surfaceCreated: url >> $url")
        thread { play(url, holder.surface) }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    private external fun testFfmpeg(): String

    private external fun play(url: String, surface: Surface): Int

}