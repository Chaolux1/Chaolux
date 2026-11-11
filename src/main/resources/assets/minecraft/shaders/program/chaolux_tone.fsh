#version 150
uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform vec2 OutSize;
uniform float Day;
in vec2 texCoord;
out vec4 colorVec;
float light(vec3 color) {
    return dot(color,vec3(0.21,0.71,0.07));
}
const vec3 chaos=vec3(0.6,0.0,0.8);
const vec3 lux=vec3(1.0,0.87,0.18);
const vec3 cold=vec3(0.1,0.23,0.52);
vec3 setSaturation(vec3 color,float value) {
    float lightValue=light(color);
    return mix(vec3(lightValue),color,value);
}
vec3 setContrast(vec3 color, float value) {
    return(color - 0.5) * value + 0.5;
}
void main() {
    vec4 source=texture(DiffuseSampler,texCoord);
    vec3 current=source.rgb;
    float currentLight=light(current);
    float shadow=1.0 - smoothstep(0.1,0.48,currentLight);
    float highlight=smoothstep(0.55,0.96,currentLight);
    float tone=1.0 - abs(currentLight * 2.0 - 1.0);
    vec3 dayColor=pow(max(current,vec3(0.0)),vec3(0.91));
    dayColor *= vec3(1.1,1.05,0.85);
    dayColor=setContrast(dayColor,1.1);
    dayColor=setSaturation(dayColor,1.2);
    dayColor += lux * (0.02 + highlight * 0.03 + tone * 0.08);
    dayColor += vec3(0.02,0.01,-0.01) * (0.35 + tone * 0.65);
    dayColor += chaos * shadow * 0.004;
    dayColor *= 1.0 + highlight * 0.02;
    vec3 nightColor=pow(max(current,vec3(0.0)),vec3(1.03));
    nightColor *= vec3(0.82,0.88,1.1);
    nightColor=setContrast(nightColor,1.0);
    nightColor=setSaturation(nightColor,1.14);
    nightColor += cold * (0.02 + shadow * 0.04 + tone * 0.01);
    nightColor += chaos * (0.01 + shadow * 0.05 + tone * 0.02);
    nightColor += lux * highlight * 0.01;
    nightColor *= 0.92 + highlight * 0.035;
    float day=smoothstep(0.0,1.0,Day);
    vec3 color=mix(nightColor,dayColor,day);
    vec2 center=texCoord * 2.0 - 1.0;
    center.x *=OutSize.x / max(OutSize.y,1.0);
    float vignette=smoothstep(0.95,1.75,length(center));
    float currentVignette=mix(0.07,0.042,day);
    color *= 1.0 - vignette * currentVignette;
    color=max(color,vec3(0.0));
    color=color / (vec3(1.0) + max(color - 1.0,vec3(0.0)) * 0.35);
    color=clamp(color,0.0,1.0);
    colorVec=vec4(color,1.0);
}
