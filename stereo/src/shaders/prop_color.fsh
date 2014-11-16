#version 110

uniform sampler2D backgroundTexture;
uniform sampler2D linkDistanceTexture;
uniform sampler2D sourceTexture;
uniform vec2 backgroundSize;
uniform vec2 screenTextureSize;
uniform float newStartX;

void main(){
    float distance = texture2D(linkDistanceTexture, gl_FragCoord.xy / screenTextureSize).r * 255.0;
    vec4 copyColor = texture2D(sourceTexture, gl_FragCoord.xy / screenTextureSize);
    vec4 linkedColor = texture2D(sourceTexture, vec2(gl_FragCoord.x - distance, gl_FragCoord.y) / screenTextureSize);
    vec4 backgroundColor = texture2D(backgroundTexture, gl_FragCoord.xy / backgroundSize);
    vec4 newColor = mix(linkedColor, backgroundColor, float(distance == 0.0));
    gl_FragColor = mix(copyColor, newColor, float(gl_FragCoord.x > newStartX));
}
