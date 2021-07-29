package com.blood.opencv

import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.blood.common.base.BasePermissionActivity
import com.blood.common.util.AssetsUtil
import com.blood.common.util.FileUtil
import com.blood.opencv.databinding.ActivityMainBinding
import java.io.File

class MainActivity : BasePermissionActivity(), SurfaceHolder.Callback, Camera.PreviewCallback {

    companion object {

        init {
            System.loadLibrary("native-lib")
        }

        const val TAG = "MainActivity"

        // 人脸训练集，模型
        const val FRONTALFACE = "lbpcascade_frontalface.xml"
    }

    private lateinit var binding: ActivityMainBinding
    private var cameraHelper: CameraHelper? = null
    private var cameraId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        cameraHelper?.startPreview()
    }

    override fun onPause() {
        super.onPause()
        cameraHelper?.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        opencvRelease()
    }

    override fun process() {
        val model = File(filesDir, FRONTALFACE)
        AssetsUtil.copyAssets(this, FRONTALFACE, model)

        binding.switchCamera.setOnClickListener { cameraHelper?.switchCamera() }
        binding.surfaceView.holder.addCallback(this)
        cameraHelper = CameraHelper(cameraId).apply { setPreviewCallback(this@MainActivity) }

        val outFile = File(filesDir, "recognition")
        FileUtil.deleteFile(outFile)
        opencvInit(model.absolutePath, outFile.absolutePath)
        Log.i(TAG, "process mode: ${model.absolutePath}")
        Log.i(TAG, "process out: ${outFile.absolutePath}")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        opencvSetSurface(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        opencvPostData(data, CameraHelper.WIDTH, CameraHelper.HEIGHT, cameraId)
    }

    external fun opencvInit(model: String, outPath: String)
    external fun opencvSetSurface(surface: Surface)
    external fun opencvPostData(bytes: ByteArray, w: Int, h: Int, cameraId: Int)
    external fun opencvRelease()

}