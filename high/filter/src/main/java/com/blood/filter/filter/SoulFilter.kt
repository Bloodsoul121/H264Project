package com.blood.filter.filter

import android.content.Context
import android.opengl.GLES20
import com.blood.filter.R

class SoulFilter(context: Context) : FboFilter(context, R.raw.vert_base, R.raw.frag_soul) {

    private val mixturePercent: Int = GLES20.glGetUniformLocation(program, "mixturePercent")
    private val scalePercent: Int = GLES20.glGetUniformLocation(program, "scalePercent")

    private var mix = 0.0f //透明度，越大越透明
    private var scale = 0.0f //缩放，越大就放的越大

    override fun beforeDraw() {
        GLES20.glUniform1f(mixturePercent, 1.0f - mix)
        GLES20.glUniform1f(scalePercent, 1.0f + scale)
        mix += 0.025f
        scale += 0.025f
        if (mix >= 1.0) {
            mix = 0.0f
        }
        if (scale >= 1.0) {
            scale = 0.0f
        }
    }

}