package com.blood.train;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class MyClass {

    // opencv 根据样本集，生成样本文件
    public static void main(String[] args) throws IOException {
        String path = "D:\\opencv\\lance.data";
        FileOutputStream fos = new FileOutputStream(path);
        for (int i = 0; i < 100; i++) {
            String content = String.format(Locale.US, "lance/%d.jpg 1 0 0 24 24\n", i);
            fos.write(content.getBytes());
        }
        fos.close();
    }

}