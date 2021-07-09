package com.blood.h264.parse

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blood.common.util.AssetsUtil
import com.blood.h264.databinding.ActivityH264ParseOutputBinding
import java.io.File

class H264ParseOutputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityH264ParseOutputBinding
    private lateinit var dstFilePath: String
    private var h264Parser: H264ParserOutput? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityH264ParseOutputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        copyFile()
        init()
    }

    private fun copyFile() {
        val srcFileName = "hot.h264"
        val dstFile = File(filesDir, srcFileName)
        AssetsUtil.copyAssets(this, srcFileName, dstFile)
        dstFilePath = dstFile.absolutePath
    }

    private fun init() {
        h264Parser?.stop()
        h264Parser = H264ParserOutput(this, dstFilePath, 1028, 720, object : Callback {
            override fun onBitmapCompressed(bitmap: Bitmap) {
                runOnUiThread {
                    binding.imgView.setImageBitmap(bitmap)
                }
            }

        })
        h264Parser?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        h264Parser?.stop()
    }

    interface Callback {
        fun onBitmapCompressed(bitmap: Bitmap)
    }

}