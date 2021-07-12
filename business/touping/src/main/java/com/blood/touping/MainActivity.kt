package com.blood.touping

import android.content.Intent
import android.os.Bundle
import com.blood.common.base.BasePermissionActivity
import com.blood.touping.client.ClientActivity
import com.blood.touping.push.PushActivity

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
            "client" -> startActivity(Intent(this, ClientActivity::class.java))
        }
    }

}