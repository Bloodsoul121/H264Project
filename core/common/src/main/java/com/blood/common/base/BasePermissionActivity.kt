package com.blood.common.base

import android.content.Intent
import android.os.Bundle
import com.blood.common.permission.PermissionActivity

abstract class BasePermissionActivity : BaseActivity() {

    companion object {
        const val PERMISSION_CODE = Short.MAX_VALUE.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivityForResult(Intent(this, PermissionActivity::class.java), PERMISSION_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            process()
        } else {
            finish()
        }
    }

    abstract fun process()

}