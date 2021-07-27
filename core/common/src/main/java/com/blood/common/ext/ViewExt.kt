package com.blood.common.ext

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner

fun View.getFragmentActivity(): FragmentActivity {
    var context: Context = context
    while (context !is FragmentActivity) {
        context = (context as ContextWrapper).baseContext
    }
    return context
}

fun View.getLifecycleOwner(): LifecycleOwner {
    var context: Context = context
    while (context !is LifecycleOwner) {
        context = (context as ContextWrapper).baseContext
    }
    return context
}