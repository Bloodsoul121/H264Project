package com.blood.common.permission

import android.content.pm.PackageManager
import android.os.Build
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
        checkPermissions()
    }

    private fun checkPermissions() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val requestedPermissions = packageInfo.requestedPermissions
            val denyPermissions = mutableListOf<String>()
            for (i in requestedPermissions.indices) {
                val permission = requestedPermissions[i]
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission)) {
                        denyPermissions.add(permission)
                        Log.i(TAG, "checkPermissions: deny $permission")
                    } else {
                        Log.i(TAG, "checkPermissions: granted $permission")
                    }
                }
            }
            if (denyPermissions.size > 0) {
                requestPermissions(denyPermissions)
            } else {
                setResult(RESULT_OK)
                finish()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun requestPermissions(permissions: List<String>) {
        RxPermissions(this)
                .request(*permissions.toTypedArray())
                .subscribe {
                    if (it) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        setResult(RESULT_CANCELED)
                        PermissionUtil.launchAppDetailsSettings(this)
                    }
                }
    }

}