package com.blood.filter.filter

import android.content.Context
import android.opengl.GLES20
import com.blood.filter.R

class AdaptFilter(context: Context) : BaseFilter(context, R.raw.vert_adapt, R.raw.frag_demo) {

    //变换矩阵， 需要将原本的vCoord（01,11,00,10） 与矩阵相乘
    private val vVertexMatrix: Int = GLES20.glGetUniformLocation(program, "vVertexMatrix")
    private val vTextureMatrix: Int = GLES20.glGetUniformLocation(program, "vTextureMatrix")

    override fun beforeDraw() {
        GLES20.glUniformMatrix4fv(vVertexMatrix, 1, false, vertexMatrix, 0)
        GLES20.glUniformMatrix4fv(vTextureMatrix, 1, false, textureMatrix, 0)
    }

}