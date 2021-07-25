package com.blood.filter

import android.os.Bundle
import com.blood.common.base.BasePermissionActivity
import com.blood.filter.databinding.ActivityMainBinding

class MainActivity : BasePermissionActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {

    }

}