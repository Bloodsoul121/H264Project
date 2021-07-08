package com.blood.common.permission

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.blood.common.R
import com.blood.common.util.PermissionUtil
import com.tbruyelle.rxpermissions3.RxPermissions

class PermissionActivity : AppCompatActivity() {

    companion object {
        const val TAG = "PermissionActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        requestPermissions()
    }

    private fun requestPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions.requestEach(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        ).subscribe {
            if (!it.granted) {
                Log.i(TAG, "requestPermissions deny : ${it.name}")
                if (it.shouldShowRequestPermissionRationale) {
                    PermissionUtil.launchAppDetailsSettings(this)
                }
            }
        }
    }

}