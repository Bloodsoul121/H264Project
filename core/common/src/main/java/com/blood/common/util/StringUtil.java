package com.blood.common.util;

public class StringUtil {

    public static byte[] hexStringToByteArray(String s) {
        //十六进制转byte数组
        int len = s.length();
        byte[] bs = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bs[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return bs;
    }

}
