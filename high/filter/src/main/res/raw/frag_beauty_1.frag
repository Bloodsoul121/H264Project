precision mediump float;
varying mediump vec2 aCoord;
uniform sampler2D vTexture;

//cpu传值  宽高 变量
uniform int width;
uniform int height;

//高斯模糊取样周围的20个点，然后换算平均值
vec2 blurCoordinates[20];

void main(){

    // 高斯模糊半径越小，效果越弱，半径越大，效果越强

    //步长，相当于一个像素
    vec2 singleStepOffset = vec2(1.0/float(width), 1.0/float(height));

    //取样20个点
    blurCoordinates[0] = aCoord.xy + singleStepOffset * vec2(0.0, -10.0);
    blurCoordinates[1] = aCoord.xy + singleStepOffset * vec2(0.0, 10.0);
    blurCoordinates[2] = aCoord.xy + singleStepOffset * vec2(-10.0, 0.0);
    blurCoordinates[3] = aCoord.xy + singleStepOffset * vec2(10.0, 0.0);
    blurCoordinates[4] = aCoord.xy + singleStepOffset * vec2(5.0, -8.0);
    blurCoordinates[5] = aCoord.xy + singleStepOffset * vec2(5.0, 8.0);
    blurCoordinates[6] = aCoord.xy + singleStepOffset * vec2(-5.0, 8.0);
    blurCoordinates[7] = aCoord.xy + singleStepOffset * vec2(-5.0, -8.0);
    blurCoordinates[8] = aCoord.xy + singleStepOffset * vec2(8.0, -5.0);
    blurCoordinates[9] = aCoord.xy + singleStepOffset * vec2(8.0, 5.0);
    blurCoordinates[10] = aCoord.xy + singleStepOffset * vec2(-8.0, 5.0);
    blurCoordinates[11] = aCoord.xy + singleStepOffset * vec2(-8.0, -5.0);
    blurCoordinates[12] = aCoord.xy + singleStepOffset * vec2(0.0, -6.0);
    blurCoordinates[13] = aCoord.xy + singleStepOffset * vec2(0.0, 6.0);
    blurCoordinates[14] = aCoord.xy + singleStepOffset * vec2(6.0, 0.0);
    blurCoordinates[15] = aCoord.xy + singleStepOffset * vec2(-6.0, 0.0);
    blurCoordinates[16] = aCoord.xy + singleStepOffset * vec2(-4.0, -4.0);
    blurCoordinates[17] = aCoord.xy + singleStepOffset * vec2(-4.0, 4.0);
    blurCoordinates[18] = aCoord.xy + singleStepOffset * vec2(4.0, -4.0);
    blurCoordinates[19] = aCoord.xy + singleStepOffset * vec2(4.0, 4.0);

    vec4 currentColor = texture2D(vTexture, aCoord);
    vec3 rgb = currentColor.rgb;
    for (int i = 0; i < 20; i++) {
        rgb += texture2D(vTexture, blurCoordinates[i].xy).rgb;
    }

    //高斯模糊平均值
    vec4 blur = vec4(rgb * 1.0 / 21.0, currentColor.a);

    gl_FragColor = blur;

    //由于这个程序会在每个点上计算，所以不用管其他点的赋值

}