package com.blood.h264.parse.h265

import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.blood.common.util.AssetsUtil
import com.blood.h264.databinding.ActivityH265Binding
import com.blood.h264.parse.normal.H264Parser
import java.io.File

class H265Activity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityH265Binding
    private lateinit var dstFilePath: String
    private var h264Parser: H265Parser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityH265Binding.inflate(layoutInflater)
        setContentView(binding.root)
        copyFile()
        init()
    }

    private fun copyFile() {
        val srcFileName = "normal.h265"
        val dstFile = File(filesDir, srcFileName)
        AssetsUtil.copyAssets(this, srcFileName, dstFile)
        dstFilePath = dstFile.absolutePath
    }

    private fun init() {
        binding.surfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        h264Parser?.stop()
        h264Parser = H265Parser(dstFilePath, holder.surface, 1028, 720)
        h264Parser?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        h264Parser?.stop()
    }

}