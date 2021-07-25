package com.blood.filter.filter

import android.content.Context
import android.opengl.GLES20
import com.blood.filter.util.OpenglUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class BaseFilter(context: Context, vertexShaderId: Int, fragmentShaderId: Int) {

    // 顶点坐标，世界坐标系
    private var VERTEX = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    )

    // 纹理坐标
    private var TEXTURE = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    )

    private var width = 0
    private var height = 0

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer

    protected val program: Int
    private val vPosition: Int
    private val vCoord: Int
    private val vTexture: Int

    protected var vertexMatrix: FloatArray = FloatArray(0)
    protected var textureMatrix: FloatArray = FloatArray(0)

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
    }

    fun onVertexMatrix(mtx: FloatArray) {
        this.vertexMatrix = mtx
    }

    fun onTextureMatrix(mtx: FloatArray) {
        this.textureMatrix = mtx
    }

    open fun onSizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    // 开始渲染，如果没有绑定fbo，则默认渲染到GLSurfaceView中，即屏幕上
    open fun onDraw(texture: Int): Int {
        GLES20.glViewport(0, 0, width, height)
        GLES20.glUseProgram(program)

        // 从索引位0的地方读，因为 onDraw 频繁调用，需要归零
        vertexBuffer.position(0)
        // 赋值
        // index 指定要修改的通用顶点属性的索引
        // size  指定每个通用顶点属性的组件数，每个顶点的元素个数
        // type  指定数组中每个组件的数据类型
        //       接受符号常量GL_FLOAT  GL_BYTE，GL_UNSIGNED_BYTE，GL_SHORT，GL_UNSIGNED_SHORT或GL_FIXED。
        //       初始值为GL_FLOAT。
        // normalized 指定在访问定点数据值时是应将其标准化（GL_TRUE）还是直接转换为定点值（GL_FALSE）。
        // stride 每个顶点的字节数，元素个数 * 4字节
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        // 生效，启动句柄
        GLES20.glEnableVertexAttribArray(vPosition)

        // 纹理
        textureBuffer.position(0)
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        GLES20.glEnableVertexAttribArray(vCoord)

        // 激活使用某个图层
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // 根据缓冲区生成一个采样器（2D）
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        // 第0个图层，赋值给mVTexture
        GLES20.glUniform1i(vTexture, 0)

        // 实现类进行多样化功能定制
        beforeDraw()

        // 通知画画
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        return texture
    }

    fun release() {
        GLES20.glDeleteProgram(program)
    }

    open fun beforeDraw() {}

}