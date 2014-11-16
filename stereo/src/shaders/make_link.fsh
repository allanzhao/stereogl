#version 110

uniform float pixelSep;

void main(){
    gl_FragColor = vec4(pixelSep / 255.0);
}
