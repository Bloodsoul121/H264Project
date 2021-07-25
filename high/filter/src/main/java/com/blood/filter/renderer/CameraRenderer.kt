package com.blood.filter.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import androidx.camera.core.Preview.OnPreviewOutputUpdateListener
import androidx.camera.core.Preview.PreviewOutput
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blood.filter.bean.FilterConfig
import com.blood.filter.helper.CameraXHelper
import com.blood.filter.util.LogUtil.log
import com.blood.filter.util.ToastUtil
import com.blood.filter.view.CameraView
import java.io.File
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 预览拉伸的问题，应该是本身图片撑不满全屏，然后渲染全屏后拉伸
 */
class CameraRenderer(private val cameraView: CameraView) : GLSurfaceView.Renderer, OnPreviewOutputUpdateListener, OnFrameAvailableListener {

    companion object {
        private const val TAG = "CameraRenderer"
        private const val SAVE_FILE_NAME = "record.mp4"
    }

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

    //    private MediaRecorder mMediaRecorder;
    //    private H264MediaRecorder mH264MediaRecorder;
    private var isOutput = false

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

    private fun initFilters() {
        filters.clear()
//        filters.add(FilterConfig(context, FilterConfig.FILTER_DEMO, true))
        filters.add(FilterConfig(context, FilterConfig.FILTER_ADAPT, true))
//        filters.add(FilterConfig(context, FilterConfig.FILTER_WARM, true))
//        filters.add(FilterConfig(context, FilterConfig.FILTER_BEAUTY, true))
//        filters.add(FilterConfig(context, FilterConfig.FILTER_SPLIT2, mIsSplit2FilterOpen))
//        filters.add(FilterConfig(context, FilterConfig.FILTER_SOUL, mIsSoulFilterOpen))
//        filters.add(FilterConfig(context, FilterConfig.FILTER_SCREEN, true))
    }

    private fun initMediaRecorder() {
        //录制每一帧数据
        val saveFile = File(context.filesDir, SAVE_FILE_NAME)
        val savePath = saveFile.absolutePath
        if (saveFile.exists()) {
            val delete = saveFile.delete()
            if (delete) {
                ToastUtil.toast("删除原有文件 $savePath")
            }
        }

//        mMediaRecorder = new MediaRecorder(mContext, savePath, EGL14.eglGetCurrentContext(), 480, 640);

//        mH264MediaRecorder = new H264MediaRecorder(mContext, savePath, EGL14.eglGetCurrentContext(), 480, 640);
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
//        surfaceTexture!!.getTransformMatrix(textureMatrix)

//        // 返回fbo所在的图层，还没显示到屏幕上
//        int texture = mCameraFilter.onDraw(mTextures[0]);
//
//        // 显示到屏幕上
//        texture = mRecordFilter.onDraw(texture);

        var texture = textures[0]
        filters.forEach { filter ->
            filter.filter?.onVertexMatrix(vertexMatrix)
            filter.filter?.onTextureMatrix(textureMatrix)
            texture = filter.onDraw(texture)
            Log.i(TAG, "onDrawFrame texture: $texture")
        }
        Log.i(TAG, "onDrawFrame: end")

        // 录制，还是fbo的图层，主动调用opengl方法，必须是在egl环境下，即glthread
//        if (mIsOutH264) {
//            if (mH264MediaRecorder != null) {
//                mH264MediaRecorder.fireFrame(texture, mCameraTexture.getTimestamp());
//            }
//        } else {
//            if (mMediaRecorder != null) {
//                mMediaRecorder.fireFrame(texture, mCameraTexture.getTimestamp());
//            }
//        }
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
//        if (mIsOutH264) {
//            mH264MediaRecorder.start(speed);
//        } else {
//            mMediaRecorder.start(speed);
//        }
    }

    fun stopRecord() {
//        if (mIsOutH264) {
//            mH264MediaRecorder.stop();
//        } else {
//            mMediaRecorder.stop();
//        }
    }

    fun toggleOutput(isOutput: Boolean) {
        this.isOutput = isOutput
    }

    fun toggle(id: Int) {
        // 切换到gl线程
        cameraView.queueEvent {
            for (filterConfig in filters) {
                if (filterConfig.id == id) {
                    filterConfig.toggle()
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
                    return@queueEvent
                }
            }
        }
    }

}