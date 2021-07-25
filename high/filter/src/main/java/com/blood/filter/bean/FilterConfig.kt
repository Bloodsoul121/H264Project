package com.blood.filter.bean

import android.content.Context
import com.blood.filter.filter.AdaptFilter
import com.blood.filter.filter.BaseFilter
import com.blood.filter.filter.DemoFilter

class FilterConfig(private val context: Context, val id: Int, private var isOpen: Boolean = false) {

    companion object {
        const val FILTER_DEMO = 0 //滤镜
        const val FILTER_ADAPT = 1 //滤镜，适配尺寸大小
        const val FILTER_WARM = 2 //暖色滤镜
        const val FILTER_SPLIT2 = 3 //分屏2个
        const val FILTER_SOUL = 4 //灵魂出窍
        const val FILTER_SCREEN = 5 //将数据渲染到屏幕
        const val FILTER_BEAUTY = 6 //美颜
    }

    var filter: BaseFilter? = null
        private set

    private var width = 0
    private var height = 0
    private var isInit = false

    init {
        if (isOpen) createFilter()
    }

    private fun createFilter() {
        if (isInit) {
            return
        }
        isInit = true
        filter = when (id) {
            FILTER_DEMO -> DemoFilter(context)
            FILTER_ADAPT -> AdaptFilter(context)
            else -> null
        }
    }

    fun onSizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        this.filter?.onSizeChanged(width, height)
    }

    fun onDraw(texture: Int): Int {
        if (!isOpen) {
            return texture
        }
        return filter?.onDraw(texture) ?: texture
    }

    fun toggle(): Boolean {
        isOpen = !isOpen
        if (isOpen) {
            createFilter().apply { onSizeChanged(width, height) }
        } else {
            filter?.release()
            filter = null
            isInit = false
        }
        return isOpen
    }

    fun toggle(isOpen: Boolean): Boolean {
        if (this.isOpen == isOpen) {
            return this.isOpen
        }
        return toggle()
    }

}