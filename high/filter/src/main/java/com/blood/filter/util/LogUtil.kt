package com.blood.filter.util

import android.util.Log

object LogUtil {

    private const val TAG = "LogUtil"

    fun log(msg: String) {
        Log.i(TAG, ">>> $msg")
    }

}