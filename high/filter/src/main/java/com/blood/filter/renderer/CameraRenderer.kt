package com.blood.filter.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.camera.core.Preview.OnPreviewOutputUpdateListener
import androidx.camera.core.Preview.PreviewOutput
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blood.filter.bean.FilterConfig
import com.blood.filter.helper.CameraXHelper
import com.blood.filter.record.H264MediaRecorder
import com.blood.filter.record.MediaRecorder
import com.blood.filter.util.LogUtil.log
import com.blood.filter.view.CameraView
import com.blood.filter.viewmodel.FilterViewModel
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 预览拉伸的问题，应该是本身图片撑不满全屏，然后渲染全屏后拉伸
 */
class CameraRenderer(private val cameraView: CameraView) : GLSurfaceView.Renderer, OnPreviewOutputUpdateListener, OnFrameAvailableListener {

    private var screenWidth = 0
    private var screenHeight = 0
    private var cameraWidth = 0
    private var cameraHeight = 0

    private val cameraXHelper: CameraXHelper
    private val context: Context = cameraView.context
    private val filters: MutableList<FilterConfig> = ArrayList()

    private var textures: IntArray = IntArray(0)
    private var surfaceTexture: SurfaceTexture? = null

    private val vertexMatrix = FloatArray(16) // 顶点矩阵
    private val textureMatrix = FloatArray(16) // 纹理矩阵
    private val demoMatrix = FloatArray(16) // 纹理矩阵

    private var mediaRecorder: MediaRecorder? = null
    private var h264Recorder: H264MediaRecorder? = null

    private var isH264 = false

    private var filterViewModel = ViewModelProvider(context as ViewModelStoreOwner)[FilterViewModel::class.java]

    init {
        cameraXHelper = CameraXHelper(context as LifecycleOwner, this)
        cameraXHelper.openCamera()
    }

    /**
     * 创建GLSurfaceView时，系统调用一次该方法。使用此方法执行只需要执行一次的操作，例如设置OpenGL环境参数或初始化OpenGL图形对象。
     */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        log("onSurfaceCreated")

        // 上层与GPU共享的一个id
        textures = IntArray(1)
        // 让 SurfaceTexture 与 Gpu 共享一个数据源  0-31
        // 没有赋值，其实就是0，代表是最上面的那个图层
        surfaceTexture!!.attachToGLContext(textures[0])
        //监听摄像头数据回调
        surfaceTexture!!.setOnFrameAvailableListener(this)

        initFilters()
        initMediaRecorder()
    }

    // 需要 fbo 层作为缓冲区，否则图像就直接渲染到屏幕上了
    private fun initFilters() {
        filters.clear()
//        filters.add(FilterConfig(context, FilterConfig.FILTER_DEMO, "测试", true))
        filters.add(FilterConfig(context, FilterConfig.FILTER_ADAPT, "适配尺寸", true))
//        filters.add(FilterConfig(context, FilterConfig.FILTER_WARM, true))
//        filters.add(FilterConfig(context, FilterConfig.FILTER_BEAUTY, true))
//        filters.add(FilterConfig(context, FilterConfig.FILTER_SPLIT2, mIsSplit2FilterOpen))
        filters.add(FilterConfig(context, FilterConfig.FILTER_SOUL, "灵魂出窍", true))
        filters.add(FilterConfig(context, FilterConfig.FILTER_SCREEN, "渲染屏幕", true))
        filterViewModel.notifyFilters(filters)
    }

    // 录制数据
    private fun initMediaRecorder() {
        val eglContext = EGL14.eglGetCurrentContext()
        val width = cameraHeight //480 1200
        val height = cameraWidth //640 1600
        mediaRecorder = MediaRecorder(context, eglContext, width, height)
        h264Recorder = H264MediaRecorder(context, eglContext, width, height)
    }

    /**
     * 当GLSurfaceView的发生变化时，系统调用此方法，这些变化包括GLSurfaceView的大小或设备屏幕方向的变化。
     * 例如：设备从纵向变为横向时，系统调用此方法。我们应该使用此方法来响应GLSurfaceView容器的改变。
     */
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        log("onSurfaceChanged $width $height")
        screenWidth = width
        screenHeight = height
        computeTextureMatrix()

        GLES20.glViewport(0, 0, width, height)

        Matrix.setIdentityM(vertexMatrix, 0)
        Matrix.rotateM(vertexMatrix, 0, 180f, 0f, 1f, 0f) //左右镜像
        Matrix.rotateM(vertexMatrix, 0, -90f, 0f, 0f, 1f) //旋转90

        // 通知宽高变化
        filters.forEach { filter -> filter.onSizeChanged(width, height) }
    }

    // 适配相机尺寸
    private fun computeTextureMatrix() {
        val cameraRatio = cameraWidth * 1.0f / cameraHeight
        val screenRatio = screenWidth * 1.0f / screenHeight
        Matrix.setIdentityM(textureMatrix, 0)
        if (cameraRatio > screenRatio) {
            Matrix.scaleM(textureMatrix, 0, 1f, 1 - (cameraRatio - screenRatio) / 2, 1f)
        } else if (cameraRatio < screenRatio) {
            Matrix.scaleM(textureMatrix, 0, 1 - (screenRatio - cameraRatio) / 2, 1f, 1f)
        }
    }

    /**
     * 系统在每次重画GLSurfaceView时调用这个方法。使用此方法作为绘制（和重新绘制）图形对象的主要执行方法。
     */
    override fun onDrawFrame(gl: GL10) {
        // 更新摄像头的数据，给到gpu的缓存了，不需要通过cpu传递
        surfaceTexture!!.updateTexImage()
        // 这里不是数据，获取图像数据矩阵，传值给 matrix，16个元素数组
        surfaceTexture!!.getTransformMatrix(demoMatrix)

        var texture = textures[0]
        filters.forEach { filter ->
            // 最初照相机捕获的图像需要矩阵变换，后续就以fbo的纹理缓存进行操作
            when (filter.id) {
                FilterConfig.FILTER_DEMO -> {
                    filter.filter?.onTextureMatrix(demoMatrix)
                }
                FilterConfig.FILTER_ADAPT -> {
                    filter.filter?.onVertexMatrix(vertexMatrix)
                    filter.filter?.onTextureMatrix(textureMatrix)
                }
            }
            texture = filter.onDraw(texture)
        }

        // 录制，还是fbo的图层，主动调用opengl方法，必须是在egl环境下，即glthread
        // 此时的 texture 是 fbo 层的 id
        if (isH264) {
            h264Recorder?.recordFrame(texture, surfaceTexture!!.timestamp)
        } else {
            mediaRecorder?.recordFrame(texture, surfaceTexture!!.timestamp)
        }
    }

    // 摄像头数据回调
    override fun onUpdated(output: PreviewOutput) {
        // 输出size大小，相机输出尺寸 1200×1600 与屏幕的尺寸 1080×2175 不匹配
        val size = output.textureSize
        log("onUpdated PreviewOutput size -> " + size.width + " " + size.height)
        log("onUpdated screen -> " + ScreenUtils.getAppScreenWidth() + " " + ScreenUtils.getAppScreenHeight())
        log("onUpdated real screen -> " + ScreenUtils.getScreenWidth() + " " + ScreenUtils.getScreenHeight())
        val statusBarHeight = BarUtils.getStatusBarHeight()
        val navBarHeight = BarUtils.getNavBarHeight()
        log("onUpdated bar height -> $statusBarHeight $navBarHeight")
        cameraWidth = size.width
        cameraHeight = size.height

        /*
        2021-07-25 22:14:55.992 I/LogUtil: >>> onUpdated PreviewOutput size -> 1600 1200
        2021-07-25 22:14:55.993 I/LogUtil: >>> onUpdated screen -> 1080 2175
        2021-07-25 22:14:55.994 I/LogUtil: >>> onUpdated real screen -> 1080 2400
        2021-07-25 22:14:55.996 I/LogUtil: >>> onUpdated bar height -> 95 130
        */

        // 摄像头预览到的数据 在这里
        surfaceTexture = output.surfaceTexture

        /*
         * 应用程序会先创建一个SurfaceTexture，然后将SurfaceTexture传递给图形生产者对象（比如Camera，通过调用setPreviewTexture传递），
         * 图形生产者对象生产一帧数据后，会回调onFrameAvailable通知应用程序有新的图像数据可以使用，
         * 应用程序就可以调用updateTexImage将图像数据先送到Texture，之后就可以调用opengl接口做些具体的业务了。
         *
         * 这么看来，SurfaceTexture相当于数据源与接受层之间的一个桥梁，负责传递数据
         * 传递流程：Camera -> SurfaceTexture -> SurfaceView、GLSurfaceView、TextureView -> surfaceFlinger显示
         */
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        // 一帧回调时，手动刷新，通知调用 onDrawFrame 方法
        cameraView.requestRender()
    }

    fun startRecord(speed: Float) {
        if (isH264) {
            h264Recorder?.start(speed)
        } else {
            mediaRecorder?.start(speed)
        }
    }

    fun stopRecord() {
        if (isH264) {
            h264Recorder?.stop()
        } else {
            mediaRecorder?.stop()
        }
    }

    fun toggleOutput(isOutH264: Boolean) {
        this.isH264 = isOutH264
    }

    fun toggle(id: Int) {
        // 切换到gl线程
        cameraView.queueEvent {
            for (filterConfig in filters) {
                if (filterConfig.id == id) {
                    filterConfig.toggle()
                    filterViewModel.notifyFilters(filters)
                    return@queueEvent
                }
            }
        }
    }

    fun toggle(id: Int, isOpen: Boolean) {
        // 切换到gl线程
        cameraView.queueEvent {
            for (filterConfig in filters) {
                if (filterConfig.id == id) {
                    filterConfig.toggle(isOpen)
                    filterViewModel.notifyFilters(filters)
                    return@queueEvent
                }
            }
        }
    }

}