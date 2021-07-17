#include <jni.h>
#include <string>
#include <android/log.h>
#include <pthread.h>

extern "C" {
#include  "librtmp/rtmp.h"
}

#define TAG "android_jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)


