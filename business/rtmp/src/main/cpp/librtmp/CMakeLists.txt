#关闭ssl 不支持rtmps  禁止加密验证
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")
file(GLOB rtmp_source *.c)
add_library(
        rtmp
        STATIC
        ${rtmp_source}
)

