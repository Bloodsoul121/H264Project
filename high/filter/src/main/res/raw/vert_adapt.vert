//顶点变量，存储4个变量的数组
attribute vec4 vPosition;

//接收纹理坐标，接收采样器采样图片的坐标  camera
attribute vec4 vCoord;

uniform mat4 vVertexMatrix;

uniform mat4 vTextureMatrix;

//传给片元着色器 像素点
varying vec2 aCoord;

void main(){
    // 做个相机尺寸适配
    gl_Position = vVertexMatrix * vPosition;
    aCoord = (vTextureMatrix * vCoord).xy;
}