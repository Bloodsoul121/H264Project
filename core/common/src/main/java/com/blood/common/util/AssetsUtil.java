package com.blood.common.util;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetsUtil {

    public static void copyAssets(Context context, String srcFile, String dstFile) {
        if (TextUtils.isEmpty(srcFile) || TextUtils.isEmpty(dstFile)) {
            return;
        }
        copyAssets(context, srcFile, new File(dstFile));
    }

    public static void copyAssets(Context context, String srcFile, File dstFile) {
        if (TextUtils.isEmpty(srcFile)) {
            return;
        }
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            fos = new FileOutputStream(dstFile);
            is = context.getAssets().open(srcFile);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                fos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseableUtil.close(fos);
            CloseableUtil.close(is);
        }
    }

}
