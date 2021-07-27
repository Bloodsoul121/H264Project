package com.blood.common

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

val viewModelProviderFactory = ViewModelProvider.NewInstanceFactory()

@MainThread
inline fun <reified VM : ViewModel> viewModels(viewModelStoreOwner: ViewModelStoreOwner): Lazy<VM> {
    return ViewModelLazy(VM::class, { viewModelStoreOwner.viewModelStore }, { viewModelProviderFactory })
}