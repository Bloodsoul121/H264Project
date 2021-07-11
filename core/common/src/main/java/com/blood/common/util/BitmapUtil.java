package com.blood.common.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtil {

    public static void compress(byte[] bytes, String outFile) {
        if (bytes == null || TextUtils.isEmpty(outFile)) {
            return;
        }
        File file = new File(outFile);
        if (!file.isFile()) {
            return;
        }
        compress(bytes, file);
    }

    public static void compress(byte[] bytes, File outFile) {
        if (bytes == null || outFile == null) return;
        try {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
