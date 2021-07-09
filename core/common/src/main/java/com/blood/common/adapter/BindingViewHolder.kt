package com.blood.common.adapter

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BindingViewHolder <T : ViewDataBinding>(var binding: T) : RecyclerView.ViewHolder(binding.root)