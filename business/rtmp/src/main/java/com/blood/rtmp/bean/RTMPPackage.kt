package com.blood.rtmp.bean

class RTMPPackage {

    companion object {
        const val RTMP_PACKET_TYPE_VIDEO = 0
        const val RTMP_PACKET_TYPE_AUDIO_HEAD = 1
        const val RTMP_PACKET_TYPE_AUDIO_DATA = 2
    }

    // 帧数据，这里是有包含分隔符的，在jni传输时，会减掉
    var buffer: ByteArray? = null

    // 时间戳，毫秒ms
    var tms: Long = 0

    // 视频包 音频包
    var type = 0

    constructor() {}
    constructor(buffer: ByteArray, tms: Long, type: Int) {
        this.buffer = buffer
        this.tms = tms
        this.type = type
    }

    override fun toString(): String {
        return "RTMPPackage(type=$type, tms=$tms, buffer=${buffer?.contentToString()})"
    }

}