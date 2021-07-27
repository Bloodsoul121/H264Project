package com.blood.filter

import android.content.Intent
import android.os.Bundle
import com.blood.common.base.BasePermissionActivity

class SplashActivity : BasePermissionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun process() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}