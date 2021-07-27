package com.blood.filter.bean

import android.content.Context
import com.blood.filter.filter.*

class FilterConfig(private val context: Context, val id: Int, val title: String, var isOpen: Boolean = false) {

    companion object {
        private var id = 0
        private fun genId() = id++

        val FILTER_DEMO = genId() //滤镜
        val FILTER_ADAPT = genId() //滤镜，适配尺寸大小
        val FILTER_WARM = genId() //暖色滤镜
        val FILTER_SPLIT2 = genId() //分屏2个
        val FILTER_SPLIT3 = genId() //分屏3个
        val FILTER_SOUL = genId() //灵魂出窍
        val FILTER_SCREEN = genId() //将数据渲染到屏幕
        val FILTER_BEAUTY1 = genId() //美颜1
        val FILTER_BEAUTY2 = genId() //美颜2
        val FILTER_BEAUTY3 = genId() //美颜3
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
            FILTER_WARM -> WarmFilter(context)
            FILTER_SPLIT2 -> Split2Filter(context)
            FILTER_SPLIT3 -> Split3Filter(context)
            FILTER_SOUL -> SoulFilter(context)
            FILTER_SCREEN -> ScreenFilter(context)
            FILTER_BEAUTY1 -> Beauty1Filter(context)
            FILTER_BEAUTY2 -> Beauty2Filter(context)
            FILTER_BEAUTY3 -> Beauty3Filter(context)
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