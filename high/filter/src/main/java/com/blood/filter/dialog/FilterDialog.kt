package com.blood.filter.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blood.common.adapter.BindingCallback
import com.blood.filter.adapter.FilterAdapter
import com.blood.filter.bean.FilterConfig
import com.blood.filter.databinding.LayoutDialogFilterBinding
import com.blood.filter.viewmodel.FilterViewModel

class FilterDialog private constructor() : DialogFragment() {

    private lateinit var binding: LayoutDialogFilterBinding
    private lateinit var adapter: FilterAdapter
    private lateinit var callback: BindingCallback<FilterConfig>
    private lateinit var filterViewModel: FilterViewModel

    companion object {
        fun newInstance(callback: BindingCallback<FilterConfig>): FilterDialog {
            val dialog = FilterDialog()
            dialog.callback = callback
            return dialog
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutDialogFilterBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    private fun init() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = FilterAdapter(callback).apply { adapter = this }
        filterViewModel = ViewModelProvider(requireActivity())[FilterViewModel::class.java]
        filterViewModel.onNotifyFilters.observe(this, adapter::update)
    }

}