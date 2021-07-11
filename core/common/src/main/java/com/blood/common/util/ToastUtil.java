package com.blood.common.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    public static void toast(Context context, String msg) {
        Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        toast.setText(msg);
        toast.show();
    }

}
