package com.blood.h264.parse.columbus;

/**
 * .idea -> gradle.xml -> <option name="delegatedBuild" value="false" /> -> sync
 */
public class ColumbusTest {

    public static void main(String[] args) {
        // 000 00101 -> 5 - 1 -> 4
        System.out.println(parse((byte) (5 & 0xff), 3));
    }

    private static void print(String msg) {
        System.out.println(msg);
    }

    public static int parse(byte data, int startBit) {
        // 0x80 -> 1000 0000

        // 先计算出0的个数
        int zeroCount = 0;
        while (startBit < 8) {
            if ((data & (0x80 >> (startBit % 8))) == 0) {
                zeroCount++;
                startBit++;
            } else {
                break;
            }
        }

        print("parse zeroCount : " + zeroCount);

        // 切换到1的后一位
        startBit++;

        // 根据0的个数算出值
        int result = 0;
        for (int i = 0; i < zeroCount; i++) {
            result <<= 1;
            result += (data & (0x80 >> (startBit % 8))) == 0 ? 0 : 1;
            startBit++;
        }
        result += (1 << zeroCount); // 加上开头的那个1
        result -= 1; // 根据哥伦布编码需要在结果上减一

        print("next startBit : " + startBit);

        return result;
    }

}
