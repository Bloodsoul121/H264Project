package com.blood.common.helper

import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.blood.common.util.YuvUtil

class Camera1Helper(val callback: Callback) : SurfaceHolder.Callback, Camera.PreviewCallback {

    companion object {
        const val TAG = "Camera1Helper"
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var buffer: ByteArray
    private lateinit var camera: Camera
    private lateinit var previewSize: Camera.Size
    private var nv21_rotated = ByteArray(0)
    private var nv12 = ByteArray(0)

    interface Callback {
        fun onSize(width: Int, height: Int)
        fun onCaptureData(bytes: ByteArray, width: Int, height: Int)
    }

    fun init(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView
        this.surfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.i(TAG, "surfaceCreated: ")
        openCamera()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.i(TAG, "surfaceChanged: ")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.i(TAG, "surfaceDestroyed: ")
        camera.release()
    }

    private fun openCamera() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        camera.setPreviewDisplay(surfaceView.holder)
        camera.setDisplayOrientation(90) // surface旋转了，但是数据并未旋转

        val parameters: Camera.Parameters = camera.parameters
        previewSize = parameters.previewSize
        buffer = ByteArray(previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8)
        camera.addCallbackBuffer(buffer) // 只能拿一次，要不断的调用
        camera.setPreviewCallbackWithBuffer(this)

        nv21_rotated = ByteArray(buffer.size)

        callback.onSize(previewSize.height, previewSize.width)

        val focusModeList = parameters.supportedFocusModes
        for (focusMode in focusModeList) { //检查支持的对焦
            if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            } else if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }
        }
        camera.parameters = parameters

        camera.startPreview()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        data ?: return
        YuvUtil.nv21_rotate_to_90(data, nv21_rotated, previewSize.width, previewSize.height)
        nv12 = YuvUtil.nv21toNV12(nv21_rotated)
        callback.onCaptureData(nv12, previewSize.height, previewSize.width)
        camera?.addCallbackBuffer(buffer)
    }

}