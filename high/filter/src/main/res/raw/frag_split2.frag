varying vec2 aCoord;
uniform sampler2D vTexture;
void main(){
    highp vec2 textureCoord = aCoord;
    if(textureCoord.y < 0.5) {
        textureCoord.y += 0.25;
    } else {
        textureCoord.y -= 0.25;
    }
    gl_FragColor = texture2D(vTexture, textureCoord);
}