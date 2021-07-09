package com.blood.common.base

import android.content.Intent
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blood.common.adapter.BindingCallback
import com.blood.common.adapter.MainAdapter
import com.blood.common.adapter.MainBean
import java.util.*

abstract class BaseMainActivity<T : ViewDataBinding> : BasePermissionActivity(), BindingCallback<MainBean> {

    lateinit var binding: T
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate()
        setContentView(binding.root)
        init()
    }

    private fun init() {
        recyclerView = getRecyclerView()
        val mainAdapter = MainAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mainAdapter
        recyclerView.setHasFixedSize(true)

        val list: ArrayList<MainBean> = ArrayList()
        initData(list)
        mainAdapter.update(list)
    }

    override fun onItemClick(t: MainBean) {
        startActivity(Intent(this, t.clazz))
    }

    override fun process() {}

    abstract fun inflate(): T

    abstract fun getRecyclerView(): RecyclerView

    abstract fun initData(list: ArrayList<MainBean>)

}