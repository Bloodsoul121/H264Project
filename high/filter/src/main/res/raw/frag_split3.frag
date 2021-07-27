varying vec2 aCoord;
uniform sampler2D vTexture;
void main(){
    highp vec2 textureCoord = aCoord;
    float offset = 1.0/3.0;
    if(textureCoord.y < offset) {
        textureCoord.y += offset;
    } else if(textureCoord.y > offset * 2.0) {
        textureCoord.y -= offset;
    }
    gl_FragColor = texture2D(vTexture, textureCoord);
}