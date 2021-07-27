package com.blood.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ImageFilter(context: Context, vertexShaderId: Int, fragmentShaderId: Int) {

    // 顶点坐标，世界坐标系
    private var VERTEX = floatArrayOf(
            -1.0f, 1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
    )

    // 纹理坐标
    private var TEXTURE = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    )

    // 顶点坐标，世界坐标系
    private var VERTEX2 = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
    )

    // 纹理坐标
    private var TEXTURE2 = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
    )

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer

    private val program: Int
    private val vPosition: Int
    private val vCoord: Int
    private val vTexture: Int

    init {
        // 申请gpu内存空间，顶点
        vertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        vertexBuffer.clear()
        vertexBuffer.put(VERTEX)

        // 申请gpu内存空间，纹理
        textureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        textureBuffer.clear()
        textureBuffer.put(TEXTURE)

        // 读取顶点程序和片元程序，本地文件
        val vertexShader = OpenglUtil.readRawTextFile(context, vertexShaderId)
        val fragShader = OpenglUtil.readRawTextFile(context, fragmentShaderId)
        program = OpenglUtil.loadProgram(vertexShader, fragShader)

        // 顶点坐标数组
        vPosition = GLES20.glGetAttribLocation(program, "vPosition")
        // 接收纹理坐标，接收采样器采样图片的坐标
        vCoord = GLES20.glGetAttribLocation(program, "vCoord")
        // 片元着色器，采样器
        vTexture = GLES20.glGetUniformLocation(program, "vTexture")

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun initTexture(bitmap: Bitmap): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        return textures[0]
    }

    // 开始渲染，如果没有绑定fbo，则默认渲染到GLSurfaceView中，即屏幕上
    fun onDraw(texture: Int): Int {
        GLES20.glUseProgram(program)

        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(vPosition)

        textureBuffer.position(0)
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        GLES20.glEnableVertexAttribArray(vCoord)

        // 激活使用某个图层
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // 根据缓冲区生成一个采样器（2D）
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        // 第0个图层，赋值给mVTexture
        GLES20.glUniform1i(vTexture, 0)

        // 通知画画
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(vPosition)
        GLES20.glDisableVertexAttribArray(vCoord)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        GLES20.glDisable(GLES20.GL_BLEND)

        return texture
    }

    fun release() {
        GLES20.glDeleteProgram(program)
    }

}