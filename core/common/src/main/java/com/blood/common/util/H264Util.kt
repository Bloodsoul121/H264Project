package com.blood.common.util

class H264Util {

    companion object {

        fun findNextFrame(bytes: ByteArray?, startIndex: Int): Int {
            bytes ?: return -1
            if (startIndex >= bytes.size) return -1
            val start = if (startIndex < 0) 0 else startIndex
            for (index in start until bytes.size) {
                if (bytes[index].toInt() == 0x00
                        && bytes[index + 1].toInt() == 0x00
                        && bytes[index + 2].toInt() == 0x00
                        && bytes[index + 3].toInt() == 0x01) {
                    return index
                }
            }
            return -1
        }

    }

}