package com.blood.common.helper;

public class H264Parser {

    // 位索引，如果是 h264 码流，需要考虑到 0x 00 00 00 01 的偏移位
    private int startBit = 0;

    public H264Parser() {
    }

    public H264Parser(int startBit) {
        this.startBit = startBit;
    }

    // 直接取BitCount位，转十进制
    public int u(byte[] buf, int bitCount) {
        int dwRet = 0;
        for (int i = 0; i < bitCount; i++) {
            dwRet <<= 1;
            if ((buf[startBit / 8] & (0x80 >> (startBit % 8))) != 0) {
                dwRet += 1;
            }
            startBit++;
        }
        return dwRet;
    }

    public int ue(byte[] bytes) {
        return ue(bytes, bytes.length);
    }

    // 哥伦布编码
    public int ue(byte[] bytes, int byteCount) {
        // 0x80 -> 1000 0000

        // 先计算出0的个数
        int zeroCount = 0;
        while (startBit < byteCount * 8) {
            if ((bytes[startBit / 8] & (0x80 >> (startBit % 8))) == 0) {
                zeroCount++;
                startBit++;
            } else {
                break;
            }
        }

        // 切换到1的后一位
        startBit++;

        // 根据0的个数算出值
        int result = 0;
        for (int i = 0; i < zeroCount; i++) {
            result <<= 1;
            result += (bytes[startBit / 8] & (0x80 >> (startBit % 8))) == 0 ? 0 : 1;
            startBit++;
        }
        result += (1 << zeroCount); // 加上开头的那个1
        result -= 1; // 根据哥伦布编码需要在结果上减一

        return result;
    }

    public int se(byte[] bytes) {
        return se(bytes, bytes.length);
    }

    public int se(byte[] bytes, int byteCount) {
        int UeVal = ue(bytes, byteCount);
        int nValue = (int) Math.ceil((double) UeVal / 2);
        if (UeVal % 2 == 0) {
            nValue = -nValue;
        }
        return nValue;
    }

}
