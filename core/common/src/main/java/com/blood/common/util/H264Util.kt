package com.blood.common.util

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException

class H264Util {

    companion object {

        private const val TAG = "H264Util"

        private val HEX_CHAR_TABLE = charArrayOf(
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        )

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

        fun writeContent(array: ByteArray, saveFile: File?) {
            val sb = StringBuilder()
            for (b in array) {
                // 高位
                sb.append(HEX_CHAR_TABLE[(b.toInt() and 0xf0) shr 4])
                // 低位
                sb.append(HEX_CHAR_TABLE[b.toInt() and 0x0f])
            }
            Log.i(TAG, "writeContent: $sb")
            saveFile ?: return
            var writer: FileWriter? = null
            try {
                // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
                writer = FileWriter(saveFile, true)
                writer.write(sb.toString())
                writer.write("\n")
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                CloseableUtil.close(writer)
            }
        }

        fun writeBytes(array: ByteArray?, saveFile: File?) {
            saveFile ?: return
            var writer: FileOutputStream? = null
            try {
                // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
                writer = FileOutputStream(saveFile, true)
                writer.write(array)
//                writer.write('\n'.toInt())
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                CloseableUtil.close(writer)
            }
        }

    }

}