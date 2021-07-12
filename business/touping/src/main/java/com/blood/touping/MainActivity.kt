package com.blood.touping

import android.content.Intent
import android.os.Bundle
import com.blood.common.base.BasePermissionActivity
import com.blood.touping.client.ClientActivity
import com.blood.touping.databinding.ActivityMainBinding
import com.blood.touping.push.PushActivity

class MainActivity : BasePermissionActivity() {

    companion object {
        const val SOCKET_PORT = 10000
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
        binding.reenter.setOnClickListener { skip() }
        skip()
    }

    private fun skip() {
        when (BuildConfig.type) {
            "push" -> startActivity(Intent(this, PushActivity::class.java))
            "client" -> startActivity(Intent(this, ClientActivity::class.java))
        }
    }

}