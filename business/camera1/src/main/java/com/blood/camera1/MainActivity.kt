package com.blood.camera1

import android.graphics.ImageFormat
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import com.blood.camera1.databinding.ActivityMainBinding
import com.blood.common.base.BasePermissionActivity
import com.blood.common.util.ToastUtil
import com.blood.common.util.YuvUtil
import java.io.File

class MainActivity : BasePermissionActivity(), SurfaceHolder.Callback, Camera.PreviewCallback {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var buffer: ByteArray
    private lateinit var camera: Camera
    private lateinit var previewSize: Camera.Size
    private var isCapture = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
        binding.surfaceView.holder.addCallback(this)
        binding.capture.setOnClickListener {
            if (isCapture) return@setOnClickListener
            isCapture = true
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        openCamera()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera.release()
    }

    private fun openCamera() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        camera.setPreviewDisplay(binding.surfaceView.holder)
        camera.setDisplayOrientation(90) // surface旋转了，但是数据并未旋转

        val parameters: Camera.Parameters = camera.parameters
        previewSize = parameters.previewSize
        buffer = ByteArray(previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8)
        camera.addCallbackBuffer(buffer) // 只能拿一次，要不断的调用
        camera.setPreviewCallbackWithBuffer(this)

        val focusModeList = parameters.supportedFocusModes
        for (focusMode in focusModeList) { //检查支持的对焦
            if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            } else if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }
        }

        camera.startPreview()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        doCapture(data)
        camera?.addCallbackBuffer(buffer)
    }

    private fun doCapture(bytes: ByteArray?) {
        bytes ?: return
        if (isCapture) {
            ToastUtil.toast(this, "正在拍照")
            captureNv21(bytes)
            isCapture = false
        }
    }

    private fun captureNv21(bytes: ByteArray?) {
        bytes ?: return
        YuvUtil.compressNv21(bytes, previewSize.width, previewSize.height, File(filesDir, "camera1.png"))
        ToastUtil.toast(this, "拍照成功")
    }

}