package com.blood.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Triangle : GLSurfaceView.Renderer {

    companion object {

        // coordinates 坐标
        // vertex 顶点

        // number of coordinates per vertex in this array
        // 数组中每个顶点的坐标数
        private const val COORDS_PER_VERTEX = 3

        // 每个顶点字节数
        private const val VERTEX_STRIDE = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

        // gl sl 语言，一种可执行程序，在GPU执行
        // attribute 表示声明一个变量
        // vec4 变量类型，类似 float
        private const val vertexShaderCode = """
                attribute vec4 vPosition;
                void main() {
                    gl_Position = vPosition;
                }
            """

        // precision mediump 代表使用中精度（float）
        private const val fragmentShaderCode = """
                precision mediump float;
                uniform vec4 vColor;
                void main() {  
                    gl_FragColor = vColor;
                }
            """

    }

    // in counterclockwise order:
    private val triangleCoords = floatArrayOf(
            0.0f, 0.5f, 0.0f,  // top
            -0.5f, -0.5f, 0.0f,  // bottom left
            0.5f, -0.5f, 0.0f // bottom right
    )

    // Set color with red, green, blue and alpha (opacity) values
    private val color = floatArrayOf(255f, 0f, 0f, 1.0f)

    // 顶点个数
    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX

    private var vertexBuffer: FloatBuffer? = null
    private var program = 0
    private var vPosition = 0
    private var vColor = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)

        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个float占4个字节
        val bb = ByteBuffer.allocateDirect(triangleCoords.size * 4)
        // 数组排列用nativeOrder，GPU整理内存
        bb.order(ByteOrder.nativeOrder())
        // 从ByteBuffer创建一个浮点缓冲区，bb作为参数，然后GPU申请内存，返回一个新数组
        vertexBuffer = bb.asFloatBuffer()
        // 将坐标添加到FloatBuffer
        vertexBuffer?.put(triangleCoords)
        // 设置缓冲区来读取第一个坐标
        vertexBuffer?.position(0)

        // 创建空的OpenGL ES程序
        program = GLES20.glCreateProgram()

        //数据转换
        val vertexShader: Int = GraphUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = GraphUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // 添加顶点着色器到程序中
        GLES20.glAttachShader(program, vertexShader)

        // 添加片元着色器到程序中
        GLES20.glAttachShader(program, fragmentShader)

        // 创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(program)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(program)

        // 获取顶点着色器的位置的句柄
        vPosition = GLES20.glGetAttribLocation(program, "vPosition")

        // 启用三角形顶点位置的句柄
        GLES20.glEnableVertexAttribArray(vPosition)

        //准备三角形坐标数据
        GLES20.glVertexAttribPointer(
                vPosition,
                COORDS_PER_VERTEX, // 顶点对应的坐标数
                GLES20.GL_FLOAT, // 单位
                false,
                VERTEX_STRIDE, // 顶点字节数
                vertexBuffer // 数据数组
        )

        // 获取片段着色器的颜色的句柄
        vColor = GLES20.glGetUniformLocation(program, "vColor")

        // 设置绘制三角形的颜色
        // count 代表颜色的个数，如果是多个，就是渐变颜色
        GLES20.glUniform4fv(vColor, 1, color, 0)

        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(vPosition)
    }
}