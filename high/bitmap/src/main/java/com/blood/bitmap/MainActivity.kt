package com.blood.bitmap

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blood.bitmap.databinding.ActivityMainBinding
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageFilter: ImageFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        imageFilter.release()
    }

    private fun init() {
        binding.glSurfaceView.setEGLContextClientVersion(2)
        binding.glSurfaceView.setRenderer(this)
        binding.glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        imageFilter = ImageFilter(this, R.raw.vert_base, R.raw.frag_base)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        imageFilter.onDraw(0)
    }

}