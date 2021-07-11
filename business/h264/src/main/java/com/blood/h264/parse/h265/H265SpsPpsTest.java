package com.blood.h264.parse.h265;

import com.blood.common.helper.H264SpsPpsHelper;
import com.blood.common.util.StringUtil;

import java.util.Arrays;

public class H265SpsPpsTest {

    public static void main(String[] args) {
        // 0x40 -> 00101000
        // 0x7E -> 01111110
        String hex = "";
        hex = hex.replace(" ", "");
        byte[] bytes = StringUtil.hexStringToByteArray(hex);

        H264SpsPpsHelper spsPpsHelper = new H264SpsPpsHelper();
        spsPpsHelper.parse(bytes);
        int[] size = spsPpsHelper.getSize();
        System.out.println(spsPpsHelper);
        System.out.println(Arrays.toString(size));
    }

/*
1080 2150


*/

}
