package com.blood.filter.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blood.filter.bean.FilterConfig

class FilterViewModel : ViewModel() {

    val onNotifyFilters = MutableLiveData<List<FilterConfig>>()

}