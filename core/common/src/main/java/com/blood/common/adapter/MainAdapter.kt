package com.blood.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.blood.common.R
import com.blood.common.databinding.LayoutItemMainBinding

class MainAdapter(val callback: BindingCallback<MainBean>) : RecyclerView.Adapter<BindingViewHolder<LayoutItemMainBinding>>(), View.OnClickListener {

    val list: MutableList<MainBean> = mutableListOf()

    fun update(data: List<MainBean>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<LayoutItemMainBinding> {
        val binding = DataBindingUtil.inflate<LayoutItemMainBinding>(LayoutInflater.from(parent.context), R.layout.layout_item_main, parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<LayoutItemMainBinding>, position: Int) {
        holder.binding.text.text = list[position].title
        holder.binding.root.tag = list[position]
        holder.binding.root.setOnClickListener(this)
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onClick(v: View?) {
        val bean = (v?.tag ?: return) as MainBean
        callback.onItemClick(bean)
    }

}