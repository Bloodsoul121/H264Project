cmake_minimum_required(VERSION 3.10.2)
project("nativedemo")

#将所有cpp文件定义为一个变量
file(GLOB cpp_source *.cpp)

#头文件成功引入
include_directories(${CMAKE_SOURCE_DIR}/opencv/include)

#静态库 FFmpeg
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/opencv/lib/${ANDROID_ABI}")

add_library(
        native-lib
        SHARED
        ${cpp_source}
)

find_library(
        log-lib
        log # 查找系统库
)

target_link_libraries(
        native-lib
        ${log-lib}
        opencv_java4
        jnigraphics
        android
)