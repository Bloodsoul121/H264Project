//所有float类型数据的精度是lowp
precision mediump float;
//坐标
varying vec2 aCoord;
//采样器  uniform static
uniform sampler2D vTexture;
void main(){
    //Opengl 自带函数
    vec4 rgba = texture2D(vTexture, aCoord);
    //暖色滤镜
    gl_FragColor = rgba + vec4(0.1, 0.1, 0.0, 0.0);
}