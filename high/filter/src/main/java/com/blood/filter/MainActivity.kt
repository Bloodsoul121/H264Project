package com.blood.filter

import android.os.Bundle
import com.blood.common.adapter.BindingCallback
import com.blood.common.base.BasePermissionActivity
import com.blood.filter.bean.FilterConfig
import com.blood.filter.databinding.ActivityMainBinding
import com.blood.filter.dialog.FilterDialog

class MainActivity : BasePermissionActivity(), BindingCallback<FilterConfig> {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
        binding.btn.setOnClickListener {
            FilterDialog.newInstance(this).show(supportFragmentManager, "FilterDialog")
        }
    }

    override fun onItemClick(t: FilterConfig) {
        binding.glSurface.toggle(t.id)
    }

}