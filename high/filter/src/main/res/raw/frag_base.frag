precision mediump float;// 数据精度

// varying 易变变量，顶点着色器输出数据
varying vec2 aCoord;

// uniform 是一致变量，着色器执行期间一致变量的值是不变的
uniform sampler2D vTexture;// samplerExternalOES: 图片， 采样器

void main(){
    // texture2D: vTexture采样器，采样  aCoord 这个像素点的RGBA值
    vec4 rgba = texture2D(vTexture, aCoord);//rgba
    // gl_FragColor = vec4(1.-rgba.r,1.-rgba.g,1.-rgba.b,rgba.a);
    gl_FragColor = rgba;
}