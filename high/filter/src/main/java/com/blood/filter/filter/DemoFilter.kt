package com.blood.filter.filter

import android.content.Context
import android.opengl.GLES20
import com.blood.filter.R

class DemoFilter(context: Context) : FboFilter(context, R.raw.vert_demo, R.raw.frag_demo) {

    // 变换矩阵， 需要将原本的vCoord（01,11,00,10）与矩阵相乘
    private val vTextureMatrix: Int = GLES20.glGetUniformLocation(program, "vTextureMatrix")

    override fun beforeDraw() {
        GLES20.glUniformMatrix4fv(vTextureMatrix, 1, false, textureMatrix, 0)
    }

}