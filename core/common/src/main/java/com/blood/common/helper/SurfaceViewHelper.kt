package com.blood.common.helper

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView

class SurfaceViewHelper(private val callback: Callback) : SurfaceHolder.Callback {

    interface Callback {
        fun onSurfaceCreated(surface: Surface)
        fun onSurfaceDestroyed()
    }

    fun init(surfaceView: SurfaceView) {
        surfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        callback.onSurfaceCreated(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        callback.onSurfaceDestroyed()
    }

}