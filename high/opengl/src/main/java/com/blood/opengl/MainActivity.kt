package com.blood.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import com.blood.common.base.BasePermissionActivity
import com.blood.opengl.databinding.ActivityMainBinding
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : BasePermissionActivity(), GLSurfaceView.Renderer {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
        binding.glSurface.setEGLContextClientVersion(2)
//        binding.glSurface.setRenderer(this)
        binding.glSurface.setRenderer(Triangle())
        binding.glSurface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // 类似于 onDraw
    }

}