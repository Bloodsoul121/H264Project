package com.blood.filter.filter

import android.content.Context
import android.opengl.GLES20
import com.blood.filter.R

class Beauty3Filter(context: Context) : FboFilter(context, R.raw.vert_base, R.raw.frag_beauty_3) {

    private val mVWidth: Int = GLES20.glGetUniformLocation(program, "width")
    private val mVHeight: Int = GLES20.glGetUniformLocation(program, "height")

    override fun beforeDraw() {
        GLES20.glUniform1i(mVWidth, width)
        GLES20.glUniform1i(mVHeight, height)
    }

}