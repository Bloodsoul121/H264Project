precision mediump float;
varying vec2 aCoord;
uniform sampler2D vTexture;
uniform lowp float mixturePercent;
uniform highp float scalePercent;
void main(){
    lowp vec4 textureColor = texture2D(vTexture, aCoord);
    highp vec2 textureCoordUse = aCoord;

    // 纹理中心点
    highp vec2 center = vec2(0.5, 0.5);

    // 换算放大点的坐标，即某个点在放大之后，移动到了当前点坐标
    textureCoordUse -= center;
    textureCoordUse = textureCoordUse / scalePercent;
    textureCoordUse += center;

    // 取样放大之前的点的坐标的颜色值
    lowp vec4 textureColor2 = texture2D(vTexture, textureCoordUse);

    // 线性混合两种颜色，mixturePercent [0-1]，值越大，第二个参数占比就越大
    gl_FragColor = mix(textureColor, textureColor2, mixturePercent);
}