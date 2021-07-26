package com.blood.filter.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaRecorder {

    private Context mContext;
    private String mPath;
    private int mWidth;
    private int mHeight;
    private EGLContext mGlContext;
    private float mSpeed;
    private MediaCodec mMediaCodec;
    private Surface mSurface;
    private MediaMuxer mMediaMuxer;
    private Handler mHandler;
    private boolean mIsStart;
    private EGLEnv mEglEnv;
    private int trackId;
    private long lastTimeStamp;

    public MediaRecorder(Context context, EGLContext glContext, int width, int height) {
        mContext = context.getApplicationContext();
        mPath = new File(context.getFilesDir(), "record.mp4").getAbsolutePath();
        mWidth = width;
        mHeight = height;
        mGlContext = glContext;
    }

    public void onSizeChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void start(float speed) {
        mSpeed = speed;
        initMediaCodec();
        initMediaMuxer();
        initOpenGlEnv();
    }

    private void initMediaCodec() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight);
            //颜色空间 从 surface当中获得
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            //码率
            format.setInteger(MediaFormat.KEY_BIT_RATE, 1500_000);
            //帧率
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
            //关键帧间隔
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
            //创建编码器
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            //配置编码器
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            //输入数据     byte[]   gpu  类似 mediaprojection
            mSurface = mMediaCodec.createInputSurface();
            //开启编码
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMediaMuxer() {
        //混合器 (复用器) 将编码的h.264封装为mp4
        try {
            mMediaMuxer = new MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initOpenGlEnv() {
        //創建OpenGL 的 環境
        HandlerThread handlerThread = new HandlerThread("codec-gl");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        mHandler.post(() -> {
            // 构建 opengl 环境
            mEglEnv = new EGLEnv(mContext, mGlContext, mSurface, mWidth, mHeight);
            mIsStart = true;
        });
    }

    public void stop() {
        mIsStart = false;
        mHandler.post(() -> {
            codec(true);
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
            mEglEnv.release();
            mEglEnv = null;
            mSurface = null;
            mHandler.getLooper().quitSafely();
            mHandler = null;
        });
    }

    // 主动去拿取 opengl fbo 的数据
    public void recordFrame(int texture, long timestamp) {
        if (!mIsStart) {
            return;
        }
        mHandler.post(() -> {
            // 将数据传入到 MediaCodec 提供的 surface 中
            mEglEnv.draw(texture, timestamp);
            // 编码
            codec(false);
        });
    }

    // 从MediaCodec里面取出已经解码的数据
    private void codec(boolean endOfStream) {
        //给个结束信号
        if (endOfStream) {
            mMediaCodec.signalEndOfInputStream();
        }

        while (true) {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int index = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10_000);
            if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                //如果是结束那直接退出，否则继续循环
                if (!endOfStream) {
                    break;
                }
            } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                //输出格式发生改变  第一次总会调用所以在这里开启混合器
                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                trackId = mMediaMuxer.addTrack(newFormat);
                mMediaMuxer.start();
            } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                //可以忽略
            } else {
                //调整时间戳
                bufferInfo.presentationTimeUs = (long) (bufferInfo.presentationTimeUs / mSpeed);
                //有时候会出现异常 ： timestampUs xxx < lastTimestampUs yyy for Video track
                if (bufferInfo.presentationTimeUs <= lastTimeStamp) {
                    bufferInfo.presentationTimeUs = (long) (lastTimeStamp + 1_000_000 / 25 / mSpeed);
                }
                lastTimeStamp = bufferInfo.presentationTimeUs;

                //正常则 index 获得缓冲区下标
                ByteBuffer encodedData = mMediaCodec.getOutputBuffer(index);
                //如果当前的buffer是配置信息，不管它 不用写出去
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }
                if (bufferInfo.size != 0) {
                    //设置从哪里开始读数据(读出来就是编码后的数据)
                    encodedData.position(bufferInfo.offset);
                    //设置能读数据的总长度
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    //写出为mp4
                    mMediaMuxer.writeSampleData(trackId, encodedData, bufferInfo);
                }
                // 释放这个缓冲区，后续可以存放新的编码后的数据啦
                mMediaCodec.releaseOutputBuffer(index, false);
                // 如果给了结束信号 signalEndOfInputStream
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
        }
    }

}
