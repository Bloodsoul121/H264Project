package com.blood.h264

import androidx.recyclerview.widget.RecyclerView
import com.blood.common.adapter.BindingCallback
import com.blood.common.adapter.MainBean
import com.blood.common.base.BaseMainActivity
import com.blood.h264.databinding.ActivityMainBinding
import com.blood.h264.parse.normal.H264ParseActivity
import com.blood.h264.parse.output.H264ParseOutputActivity
import com.blood.h264.parse.screen.ScreenActivity
import java.util.*

class MainActivity : BaseMainActivity<ActivityMainBinding>(), BindingCallback<MainBean> {

    override fun inflate(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun getRecyclerView(): RecyclerView {
        return binding.recyclerView
    }

    override fun initData(list: ArrayList<MainBean>) {
        list.add(MainBean("H264ParseActivity", H264ParseActivity::class.java))
        list.add(MainBean("H264ParseOutputActivity", H264ParseOutputActivity::class.java))
        list.add(MainBean("ScreenActivity", ScreenActivity::class.java))
    }

}