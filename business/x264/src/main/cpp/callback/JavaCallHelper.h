//
// Created by 蔡光祖 on 2/22/21.
//

#ifndef AUDIOVIDEODEMO_JAVACALLHELPER_H
#define AUDIOVIDEODEMO_JAVACALLHELPER_H

#include <jni.h>

//标记线程 因为子线程需要attach
#define THREAD_MAIN 1
#define THREAD_CHILD 2

class JavaCallHelper {
public:
    JavaCallHelper(JavaVM *_javaVM, JNIEnv *_env, jobject &_jobj);
    ~JavaCallHelper();

    void postH264(char *data,int length, int thread = THREAD_MAIN);
public:
    JavaVM *javaVM;
    JNIEnv *env;
    jobject jobj;
    jmethodID jmid_postData;
};

#endif //AUDIOVIDEODEMO_JAVACALLHELPER_H
