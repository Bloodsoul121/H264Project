package com.blood.common.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    private static final String TAG = "FileUtil";

    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihFile(dir);
    }

    public static void deleteDirWihFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                deleteFile(file); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteDirWihFile(file); // 递规的方式删除文件夹
            }
        }
        dir.delete();// 删除目录本身
    }

    public static void deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File file = new File(path);
        deleteFile(file);
    }

    public static void deleteFile(File file) {
        if (file == null || !file.isFile()) {
            return;
        }
        file.delete();
    }

    public static byte[] getBytes(String file) {
        if (TextUtils.isEmpty(file)) {
            Log.e(TAG, "file is null");
            return null;
        }
        return getBytes(new File(file));
    }

    public static byte[] getBytes(File file) {
        if (file == null) {
            Log.e(TAG, "File is null");
            return null;
        }
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            byte[] arr = new byte[1024];
            while ((len = dis.read(arr)) != -1) {
                bos.write(arr, 0, len);
            }
            Log.d(TAG, "getBytes size : " + bos.size());
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
