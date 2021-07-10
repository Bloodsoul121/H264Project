package com.blood.common.util

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager

class MediaProjectionUtil {

    companion object {

        fun requestMediaProject(activity: Activity, requestCode: Int): MediaProjectionManager {
            val mediaProjectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()
            activity.startActivityForResult(screenCaptureIntent, requestCode)
            return mediaProjectionManager
        }

    }

}