cmake_minimum_required(VERSION 3.10.2)
project("nativedemo")

add_subdirectory(librtmp)

#将所有cpp文件定义为一个变量
file(GLOB cpp_source *.cpp)
#file(GLOB cpp_source *.cpp util/*.cpp)

#头文件成功引入
include_directories(${CMAKE_SOURCE_DIR}/x264/${ANDROID_ABI}/include)
#include_directories(${CMAKE_SOURCE_DIR}/faac/${ANDROID_ABI}/include)
#include_directories(${CMAKE_SOURCE_DIR}/util)

#静态库 FFmpeg
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/x264/${ANDROID_ABI}/lib")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/faac/${ANDROID_ABI}/lib")

add_library(
        native-lib
        SHARED
        ${cpp_source}
)

find_library(
        log-lib
        log
)

target_link_libraries(
        native-lib
        ${log-lib}
        rtmp
        x264
)