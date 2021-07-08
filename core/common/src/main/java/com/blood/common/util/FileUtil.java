package com.blood.common.util;

import android.text.TextUtils;
import android.util.Log;

import com.blood.common.util.CloseableUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    private static final String TAG = "FileUtil";

    public static void writeContent(byte[] array, File saveFile) {
        char[] HEX_CHAR_TABLE = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            // 高位
            sb.append(HEX_CHAR_TABLE[(b & 0xf0) >> 4]);
            // 低位
            sb.append(HEX_CHAR_TABLE[b & 0x0f]);
        }
        Log.i(TAG, "writeContent: " + sb.toString());
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(saveFile, true);
            writer.write(sb.toString());
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseableUtil.close(writer);
        }
    }

    public static void writeBytes(byte[] array, File saveFile) {
        FileOutputStream writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileOutputStream(saveFile, true);
            writer.write(array);
            writer.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseableUtil.close(writer);
        }
    }

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

}
