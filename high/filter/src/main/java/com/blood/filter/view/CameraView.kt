package com.blood.filter.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.blood.filter.renderer.CameraRenderer

class CameraView : GLSurfaceView {

    enum class Speed {
        MODE_EXTRA_SLOW, MODE_SLOW, MODE_NORMAL, MODE_FAST, MODE_EXTRA_FAST
    }

    private var speed = Speed.MODE_NORMAL
    private var cameraRenderer: CameraRenderer

    constructor(context: Context?) : super(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        setEGLContextClientVersion(2)
        cameraRenderer = CameraRenderer(this).apply { setRenderer(this) }
        //注意必须在setRenderer后面
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    // 速度 时间/速度 speed小于1就是放慢 大于1就是加快
    fun setSpeed(speed: Speed) {
        this.speed = speed
    }

    fun startRecord() {
        val speed = when (this.speed) {
            Speed.MODE_EXTRA_SLOW -> 0.3f
            Speed.MODE_SLOW -> 0.5f
            Speed.MODE_NORMAL -> 1f
            Speed.MODE_FAST -> 2f
            Speed.MODE_EXTRA_FAST -> 3f
        }
        cameraRenderer.startRecord(speed)
    }

    fun stopRecord() {
        cameraRenderer.stopRecord()
    }

    fun toggleOutH264(isOutH264: Boolean) {
        cameraRenderer.toggleOutput(isOutH264)
    }

}