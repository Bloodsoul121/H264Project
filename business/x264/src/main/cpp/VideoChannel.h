#ifndef H264PROJECT_VIDEOCHANNEL_H
#define H264PROJECT_VIDEOCHANNEL_H

#include <inttypes.h>
#include <jni.h>
#include <x264.h>
#include "librtmp/rtmp.h"
#include "JavaCallHelper.h"

class VideoChannel {

    typedef void (*VideoCallback)(RTMPPacket *packet);

public:
    // 构造函数
    VideoChannel();

    // 析构函数
    ~VideoChannel();

    // 将配置信息设置进去，创建x264编码器
    void initVideoCodec(int width, int height, int fps, int bitrate);

    // 编码一帧数据
    void encodeFrame(int8_t *data);

    void sendSpsPps(uint8_t *sps, uint8_t *pps, int len, int pps_len);

    //发送帧   关键帧、非关键帧
    void sendFrame(int type, int payload, uint8_t *p_payload);

    void setVideoCallback(VideoCallback callback);

private:
    int mWidth;
    int mHeight;
    int mFps;
    int mBitrate;

    int ySize;
    int uvSize;

    // 编码器
    x264_t *videoCodec = 0;

    // yuv-->h264 平台 容器 x264_picture_t=bytebuffer
    x264_picture_t *pic_in = 0;

    VideoCallback callback;

public:
    JavaCallHelper *javaCallHelper;
};


#endif //H264PROJECT_VIDEOCHANNEL_H
