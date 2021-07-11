package com.blood.touping

import android.content.Intent
import android.os.Bundle
import com.blood.common.base.BasePermissionActivity

class MainActivity : BasePermissionActivity() {

    companion object {
        const val SOCKET_PORT = 10000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun process() {
        when (BuildConfig.type) {
            "push" -> startActivity(Intent(this, PushActivity::class.java))
            "server" -> startActivity(Intent(this, ServerActivity::class.java))
        }
    }

}