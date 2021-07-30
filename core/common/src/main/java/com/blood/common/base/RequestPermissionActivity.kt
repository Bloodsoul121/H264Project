package com.blood.common.base

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.blood.common.permission.PermissionActivity
import com.blood.common.util.PermissionUtil
import com.tbruyelle.rxpermissions3.RxPermissions

abstract class RequestPermissionActivity : BaseActivity() {

    companion object {
        const val PERMISSION_CODE = Short.MAX_VALUE.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateView()
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
                        Log.i(PermissionActivity.TAG, "checkPermissions: deny $permission")
                    } else {
                        Log.i(PermissionActivity.TAG, "checkPermissions: granted $permission")
                    }
                }
            }
            if (denyPermissions.size > 0) {
                requestPermissions(denyPermissions)
            } else {
                setResult(RESULT_OK)
                process()
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
                        process()
                    } else {
                        setResult(RESULT_CANCELED)
                        PermissionUtil.launchAppDetailsSettings(this)
                    }
                }
    }

    abstract fun onCreateView()
    abstract fun process()

}