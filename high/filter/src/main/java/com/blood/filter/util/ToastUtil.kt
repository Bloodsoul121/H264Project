package com.blood.filter.util

import android.widget.Toast
import com.blood.filter.App

object ToastUtil {

    fun toast(msg: String?) {
        App.instance.runUITask {
            val toast: Toast = Toast.makeText(App.instance, "", Toast.LENGTH_SHORT)
            toast.setText(msg)
            toast.show()
        }
    }

}