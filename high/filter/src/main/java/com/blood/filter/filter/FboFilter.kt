package com.blood.filter.filter

import android.content.Context
import android.opengl.GLES20

// 离屏缓存
abstract class FboFilter(context: Context, vertexShaderId: Int, fragmentShaderId: Int) : BaseFilter(context!!, vertexShaderId, fragmentShaderId) {

    private var frameBuffers: IntArray = IntArray(1)
    private var frameTextures: IntArray = IntArray(1)

    override fun onSizeChanged(width: Int, height: Int) {
        super.onSizeChanged(width, height)

        releaseFrame()

        // 实例化一个fbo，类似数据区域
        // n 生成n个fbo对象
        // framebuffers 入参出参对象，返回 gpu 中 fbo 的索引
        // offset 偏移
        GLES20.glGenFramebuffers(1, frameBuffers, 0)

        // 创建一个纹理图层
        GLES20.glGenTextures(1, frameTextures, 0)
        // 配置纹理
        for (i in frameTextures.indices) {
            // bind 之间是一个原子操作
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures[i])
            /*
             * 放大过滤，模糊 or 锯齿
             * param : GL_LINEAR 模糊效果 ， GL_NEAREST 锯齿效果
             */
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
            //缩小过滤
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            // end 原子操作结束，传入 0 ，代表操作完了
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }

        // 绑定操作
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures[0])

        /*
         * 指定一个二维的纹理图片
         * level
         *     指定细节级别，0级表示基本图像，n级则表示Mipmap缩小n级之后的图像（缩小2^n）
         * internalformat
         *     指定纹理内部格式，必须是下列符号常量之一：GL_ALPHA，GL_LUMINANCE，GL_LUMINANCE_ALPHA，GL_RGB，GL_RGBA。
         * width height
         *     指定纹理图像的宽高，所有实现都支持宽高至少为64 纹素的2D纹理图像和宽高至少为16 纹素的立方体贴图纹理图像 。
         * border
         *     指定边框的宽度。必须为0。
         * format
         *     指定纹理数据的格式。必须匹配internalformat。下面的符号值被接受：GL_ALPHA，GL_RGB，GL_RGBA，GL_LUMINANCE，和GL_LUMINANCE_ALPHA。
         * type
         *     指定纹理数据的数据类型。下面的符号值被接受：GL_UNSIGNED_BYTE，GL_UNSIGNED_SHORT_5_6_5，GL_UNSIGNED_SHORT_4_4_4_4，和GL_UNSIGNED_SHORT_5_5_5_1。
         * data
         *     指定一个指向内存中图像数据的指针。
         */
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)

        // 绑定fbo数据，这里只是一个原子操作的开始，与上面类似
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0])

        /*
         * 真正发生绑定，fbo 和 纹理 (图层)
         * int target,
         * int attachment,
         * int textarget,
         * int texture,
         * int level
         */
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, frameTextures[0], 0)

        // 下面两个都是解锁原子操作的
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    private fun releaseFrame() {
        GLES20.glDeleteTextures(1, frameTextures, 0)
        GLES20.glDeleteFramebuffers(1, frameBuffers, 0)
    }

    override fun onDraw(texture: Int): Int {
        // 数据渲染到fbo中，并未输出到屏幕上
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0])
        super.onDraw(texture)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        return frameTextures[0]
    }

}