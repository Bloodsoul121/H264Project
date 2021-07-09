package com.blood.common.util;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public class MediaCodecUtil {

    public static byte[] getOutputBufferBytes(MediaCodec mediaCodec, int outputBufferIndex, int limitSize) {
        ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
        outputBuffer.limit(limitSize);
        byte[] byteArray = new byte[outputBuffer.remaining()];
        outputBuffer.get(byteArray);
        return byteArray;
    }

}
