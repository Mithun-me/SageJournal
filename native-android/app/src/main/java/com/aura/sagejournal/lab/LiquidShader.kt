package com.aura.sagejournal.lab

/**
 * AGSL port of the WebGL fragment shader in
 * src/components/LiquidShaderCanvas.tsx.
 *
 * Differences from the web original, both deliberate:
 *  - Theme colours arrive as uniforms instead of being interpolated into the
 *    shader source as string literals, so switching theme no longer forces a
 *    shader recompile.
 *  - uv.y is flipped, because WebGL's texture coordinates run bottom-up while
 *    AGSL's fragCoord runs top-down.
 */
const val LIQUID_AGSL = """
uniform float  uTime;
uniform float2 uResolution;
uniform float2 uPointer;
uniform float  uIntensity;
uniform float3 uColor1;
uniform float3 uColor2;
uniform float3 uColor3;
uniform float  uBrightness;

float3 mod289_3(float3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
float2 mod289_2(float2 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
float3 permute3(float3 x) { return mod289_3(((x * 34.0) + 1.0) * x); }

float snoise(float2 v) {
    const float4 C = float4(0.211324865405187, 0.366025403784439,
                            -0.577350269189626, 0.024390243902439);
    float2 i  = floor(v + dot(v, C.yy));
    float2 x0 = v - i + dot(i, C.xx);
    float2 i1 = (x0.x > x0.y) ? float2(1.0, 0.0) : float2(0.0, 1.0);
    float4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;
    i = mod289_2(i);
    float3 p = permute3(permute3(i.y + float3(0.0, i1.y, 1.0))
                        + i.x + float3(0.0, i1.x, 1.0));
    float3 m = max(0.5 - float3(dot(x0, x0), dot(x12.xy, x12.xy), dot(x12.zw, x12.zw)), 0.0);
    m = m * m;
    m = m * m;
    float3 x = 2.0 * fract(p * C.www) - 1.0;
    float3 h = abs(x) - 0.5;
    float3 a0 = x - floor(x + 0.5);
    m *= 1.79284291400159 - 0.85373472095314 * (a0 * a0 + h * h);
    float3 g;
    g.x = a0.x * x0.x + h.x * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / uResolution;
    uv.y = 1.0 - uv.y;
    float t = uTime * 0.18;

    float2 pointerNorm = uPointer / uResolution;
    float distToPointer = distance(uv, pointerNorm);
    float pointerWave = sin(distToPointer * 10.0 - t * 2.0)
                        * exp(-distToPointer * 3.0) * 0.08;

    float n1 = snoise((uv + pointerWave) * 2.2 + t);
    float n2 = snoise(uv * 3.2 - t * 0.85);
    float n3 = snoise(uv * 4.0 + float2(n1, n2) * 0.5 * uIntensity);

    float mixFactor = (n3 + 1.0) * 0.5;
    float3 finalColor = mix(uColor1, uColor2, mixFactor);
    finalColor = mix(finalColor, uColor3, pow(abs(n1), 3.0) * 0.22);
    finalColor *= uBrightness;

    return half4(half3(finalColor), 1.0);
}
"""

/** Palettes lifted from LiquidShaderCanvas.tsx's isDeepSea branches. */
enum class AuraTheme(
    val color1: Triple<Float, Float, Float>,
    val color2: Triple<Float, Float, Float>,
    val color3: Triple<Float, Float, Float>,
    val brightness: Float,
) {
    LiquidGlass(
        color1 = Triple(0.31f, 0.27f, 0.90f),
        color2 = Triple(0.05f, 0.65f, 0.58f),
        color3 = Triple(0.88f, 0.88f, 1.00f),
        brightness = 0.45f,
    ),
    DeepSea(
        color1 = Triple(0.00f, 0.31f, 0.34f),
        color2 = Triple(0.00f, 0.42f, 0.39f),
        color3 = Triple(0.95f, 0.98f, 1.00f),
        brightness = 0.75f,
    ),
}
