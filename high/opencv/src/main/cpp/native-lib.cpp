#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <pthread.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <iostream>
#include <android/log.h>
#include <android/native_window_jni.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"native",__VA_ARGS__)

using namespace cv;

DetectionBasedTracker *tracker = nullptr;
ANativeWindow *window = nullptr;
static int index = 0;
jstring imagePath;

// 实例化适配器  方块  丢给 Adapter      图像 未知图像    关键点    提供
class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector {
public:
    CascadeDetectorAdapter() = delete;

    explicit CascadeDetectorAdapter(Ptr<CascadeClassifier> detector)
            : IDetector(), maniuDetector(std::move(detector)) {}

    // 类似 adapter 的适配方法
    void detect(const Mat &image, std::vector<cv::Rect> &objects) override {
        maniuDetector->detectMultiScale(image,
                                        objects,
                                        scaleFactor,
                                        minNeighbours,
                                        0,
                                        minObjSize,
                                        maxObjSize);
    }

private:
    // 分类器，作用 分类
    Ptr<CascadeClassifier> maniuDetector;
};

extern "C"
JNIEXPORT void JNICALL
Java_com_blood_opencv_MainActivity_opencvInit(JNIEnv *env, jobject, jstring model_,
                                              jstring outPath_) {
    const char *model = env->GetStringUTFChars(model_, nullptr);
    const char *outPath = env->GetStringUTFChars(outPath_, nullptr);
    imagePath = static_cast<jstring>(env->NewGlobalRef(outPath_));

    // 模型 比对权重，400k数据，耗时0.5-1s
    // 太过耗时，采用跟踪器，跟踪上一帧人脸的那一块数据，检测器检查是否有新人脸进来

    // 智能指针 自己实现了析构函数  opencv    实例化 所有对象
    // 分类器
    Ptr<CascadeClassifier> classifier1 = makePtr<CascadeClassifier>(model);
    // 创建一个检测器
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(classifier1);

    // 创建一个跟踪器
    Ptr<CascadeClassifier> classifier2 = makePtr<CascadeClassifier>(model);
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(classifier2);

    DetectionBasedTracker::Parameters detectorParams;
    tracker = new DetectionBasedTracker(mainDetector, trackingDetector, detectorParams);
    tracker->run();

    env->ReleaseStringUTFChars(model_, model);
    env->ReleaseStringUTFChars(outPath_, outPath);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_blood_opencv_MainActivity_opencvPostData(JNIEnv *env, jobject thiz, jbyteArray data_,
                                                  jint w,
                                                  jint h, jint cameraId) {

    jbyte *data = env->GetByteArrayElements(data_, nullptr);

    // data 数据未知的数据 图像
    // data nv21
//    Mat * mat = new Mat(h + h / 2, w, CV_8UC1, data);
    Mat src(h + h / 2, w, CV_8UC1, data);
    //颜色格式的转换 nv21->RGBA
    cvtColor(src, src, COLOR_YUV2RGBA_NV21);

    // 前置摄像头
    if (cameraId == 1) {
        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE);
        flip(src, src, 1);
    } else {
        //顺时针旋转90度
        rotate(src, src, ROTATE_90_CLOCKWISE);
    }

    // 将颜色转为灰色
    Mat gray;
    cvtColor(src, gray, COLOR_RGBA2GRAY);

    // 增强对比度 (直方图均衡)，抛光
    equalizeHist(gray, gray);

    // 输出图片
    const char *outPath = env->GetStringUTFChars(imagePath, nullptr);
    char p[100];
    mkdir(outPath, 0777);
//    sprintf(p, "%s/%d.jpg", outPath, index++);
//    imwrite(p, src);

    // 丢到适配器里面识别
    tracker->process(gray);

    std::vector<Rect> faces;
    // 检测到的结果 矩形框 -> 人脸  键盘   被子
    tracker->getObjects(faces);

    // faces 有数据  意思是识别出来了   位置
    for (const Rect &face:faces) {

        LOGI("识别出 width: %d  height: %d", face.width, face.height);

        Mat m;
        m = gray(face).clone(); // 复制给m
        resize(m, m, Size(24, 24)); // 缩小图像，生成训练样本

        sprintf(p, "%s/%d.jpg", outPath, index++);
        LOGI("save %s", p);

        imwrite(p, m);

//        原图
//        Scalar *scalar = new Scalar(0, 0, 255);
//        rectangle(src, face,*scalar);
//        Scalar(0, 0, 255)
//        rectangle(src, face, Scalar(0, 0, 255));
    }
/*//     话一个框框  释放
//    数据画到SurfaceView
    if (window) {
//        初始化了
//        画面中   window  缓冲区 设置 大小

        do {
//            if (!window) {
//                break;
//            }
            ANativeWindow_setBuffersGeometry(window, src.cols, src.rows, WINDOW_FORMAT_RGBA_8888);
//            缓冲区    得到

            ANativeWindow_Buffer buffer;
            if (ANativeWindow_lock(window, &buffer, 0)) {
                ANativeWindow_release(window);
                window = 0;
            }
//            知道为什么*4   rgba
            int srclineSize = src.cols * 4;
//目的数据
            int dstlineSize = buffer.stride * 4;

//            待显示的缓冲区
            uint8_t *dstData = static_cast<uint8_t *>(buffer.bits);
//像素的数据源
//            dstData 目的 内存    src   数据源
//for循环行数   这个
            for (int i = 0; i < buffer.height; ++i) {
                memcpy(dstData + i * dstlineSize, src.data + i * srclineSize, srclineSize);
            }
            ANativeWindow_unlockAndPost(window);
        } while (0);
    }*/
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_blood_opencv_MainActivity_opencvSetSurface(JNIEnv *env, jobject thiz, jobject surface) {
//    if (window) {
//        ANativeWindow_release(window);
//        window = nullptr;
//    }
//    // 渲染surface  --->window  --->windwo
//    window = ANativeWindow_fromSurface(env, surface);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_blood_opencv_MainActivity_opencvRelease(JNIEnv *env, jobject thiz) {

}