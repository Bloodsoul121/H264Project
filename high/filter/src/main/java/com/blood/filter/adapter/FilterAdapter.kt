package com.blood.filter.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.blood.common.adapter.BindingCallback
import com.blood.common.adapter.BindingViewHolder
import com.blood.filter.R
import com.blood.filter.bean.FilterConfig
import com.blood.filter.databinding.LayoutItemFilterBinding

class FilterAdapter(private val callback: BindingCallback<FilterConfig>) : RecyclerView.Adapter<BindingViewHolder<LayoutItemFilterBinding>>(), View.OnClickListener {

    private val list: MutableList<FilterConfig> = mutableListOf()

    fun update(data: List<FilterConfig>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<LayoutItemFilterBinding> {
        val binding = DataBindingUtil.inflate<LayoutItemFilterBinding>(LayoutInflater.from(parent.context), R.layout.layout_item_filter, parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<LayoutItemFilterBinding>, position: Int) {
        holder.binding.text.text = list[position].title
        holder.binding.root.tag = list[position]
        holder.binding.root.setOnClickListener(this)
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onClick(v: View?) {
        val bean = (v?.tag ?: return) as FilterConfig
        callback.onItemClick(bean)
    }

}