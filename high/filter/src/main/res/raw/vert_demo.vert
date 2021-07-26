//接收顶点坐标（世界坐标系），顶点变量，存储4个变量的数组
attribute vec4 vPosition;

//接收纹理坐标，接收采样器采样图片的坐标  camera
attribute vec4 vCoord;

// oepngl camera，图像矩阵
uniform mat4 vTextureMatrix;

//传给片元着色器 像素点
varying vec2 aCoord;

void main(){
    // gpu  需要渲染的 什么图像   形状
    gl_Position = vPosition;
    // 遍历的 for循环 性能比较低，所以这里是并发的
    // vMatrix * vCoord 是通过矩阵将 顶点坐标系 和 纹理坐标系 进行转换
    // 个人觉得，是把 纹理坐标 转为 顶点坐标，然后片元着色器根据坐标，去原图采样颜色，输出
    aCoord = (vTextureMatrix * vCoord).xy;
}