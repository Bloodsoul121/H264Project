package com.blood.demo

import com.blood.common.base.RequestPermissionActivity
import com.blood.demo.databinding.ActivityMainBinding

class MainActivity : RequestPermissionActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreateView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
    }

}