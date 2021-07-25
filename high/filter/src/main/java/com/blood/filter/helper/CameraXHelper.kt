package com.blood.filter.helper

import android.util.Size
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.Preview.OnPreviewOutputUpdateListener
import androidx.camera.core.PreviewConfig
import androidx.camera.core.UseCase
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.ScreenUtils

class CameraXHelper(private val lifecycleOwner: LifecycleOwner, private val listener: OnPreviewOutputUpdateListener) {

    private val preView: UseCase
        get() {
            val width = ScreenUtils.getAppScreenWidth()
            val height = ScreenUtils.getAppScreenHeight()
            // 分辨率并不是最终的分辨率，CameraX会自动根据设备的支持情况，结合你的参数，设置一个最为接近的分辨率
            val previewConfig = PreviewConfig.Builder()
                    .setTargetResolution(Size(height, width))
                    .setLensFacing(CameraX.LensFacing.BACK) //前置或者后置摄像头
                    .build()
            return Preview(previewConfig).apply { onPreviewOutputUpdateListener = listener }
        }

    fun openCamera() {
        CameraX.bindToLifecycle(lifecycleOwner, preView)
    }

}