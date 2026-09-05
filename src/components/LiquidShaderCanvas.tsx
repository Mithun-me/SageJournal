import React, { useEffect, useRef } from 'react';
import { AppTheme } from '../types';

interface LiquidShaderCanvasProps {
  theme?: AppTheme;
  intensity?: number;
  speed?: number;
}

export const LiquidShaderCanvas: React.FC<LiquidShaderCanvasProps> = ({
  theme = 'liquid-glass',
  intensity = 1.0,
  speed = 1.0,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
    if (!gl) return;

    let animationFrameId: number;

    const vsSource = `
      attribute vec2 a_position;
      varying vec2 v_texCoord;
      void main() {
        v_texCoord = a_position * 0.5 + 0.5;
        gl_Position = vec4(a_position, 0.0, 1.0);
      }
    `;

    // Colors adjusted dynamically for Liquid Glass vs Deep Sea
    const isDeepSea = theme === 'deep-sea';
    const color1 = isDeepSea ? 'vec3(0.0, 0.31, 0.34)' : 'vec3(0.31, 0.27, 0.90)'; // Teal vs Indigo
    const color2 = isDeepSea ? 'vec3(0.0, 0.42, 0.39)' : 'vec3(0.05, 0.65, 0.58)'; // Seafoam vs Electric Teal
    const color3 = isDeepSea ? 'vec3(0.95, 0.98, 1.0)' : 'vec3(0.88, 0.88, 1.0)';  // Sand / Soft white
    const brightnessFactor = isDeepSea ? '0.75' : '0.45';

    const fsSource = `
      precision highp float;
      varying vec2 v_texCoord;
      uniform float u_time;
      uniform vec2 u_resolution;
      uniform vec2 u_mouse;
      uniform float u_intensity;

      vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
      vec2 mod289(vec2 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
      vec3 permute(vec3 x) { return mod289(((x*34.0)+1.0)*x); }

      float snoise(vec2 v) {
        const vec4 C = vec4(0.211324865405187, 0.366025403784439,
                            -0.577350269189626, 0.024390243902439);
        vec2 i  = floor(v + dot(v, C.yy) );
        vec2 x0 = v -   i + dot(i, C.xx);
        vec2 i1;
        i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
        vec4 x12 = x0.xyxy + C.xxzz;
        x12.xy -= i1;
        i = mod289(i);
        vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
            + i.x + vec3(0.0, i1.x, 1.0 ));
        vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
        m = m*m;
        m = m*m;
        vec3 x = 2.0 * fract(p * C.www) - 1.0;
        vec3 h = abs(x) - 0.5;
        vec3 a0 = x - floor(x + 0.5);
        m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );
        vec3 g;
        g.x  = a0.x  * x0.x  + h.x  * x0.y;
        g.yz = a0.yz * x12.xz + h.yz * x12.yw;
        return 130.0 * dot(m, g);
      }

      void main() {
        vec2 uv = v_texCoord;
        float t = u_time * 0.18;
        
        // Liquid motion with mouse distortion
        vec2 mouseNorm = u_mouse / u_resolution;
        float distToMouse = distance(uv, mouseNorm);
        float mouseWave = sin(distToMouse * 10.0 - t * 2.0) * exp(-distToMouse * 3.0) * 0.08;

        float n1 = snoise((uv + mouseWave) * 2.2 + t);
        float n2 = snoise(uv * 3.2 - t * 0.85);
        float n3 = snoise(uv * 4.0 + vec2(n1, n2) * 0.5 * u_intensity);
        
        vec3 col1 = ${color1};
        vec3 col2 = ${color2};
        vec3 col3 = ${color3};
        
        float mixFactor = (n3 + 1.0) * 0.5;
        vec3 finalColor = mix(col1, col2, mixFactor);
        finalColor = mix(finalColor, col3, pow(abs(n1), 3.0) * 0.22);
        
        // Atmosphere balance for high UI readability
        finalColor *= ${brightnessFactor};
        
        gl_FragColor = vec4(finalColor, 1.0);
      }
    `;

    function createShader(gl: WebGLRenderingContext, type: number, source: string) {
      const shader = gl.createShader(type);
      if (!shader) return null;
      gl.shaderSource(shader, source);
      gl.compileShader(shader);
      if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
        console.error('Shader compile error:', gl.getShaderInfoLog(shader));
        gl.deleteShader(shader);
        return null;
      }
      return shader;
    }

    const vs = createShader(gl, gl.VERTEX_SHADER, vsSource);
    const fs = createShader(gl, gl.FRAGMENT_SHADER, fsSource);
    if (!vs || !fs) return;

    const program = gl.createProgram();
    if (!program) return;
    gl.attachShader(program, vs);
    gl.attachShader(program, fs);
    gl.linkProgram(program);

    if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
      console.error('Program link error:', gl.getProgramInfoLog(program));
      return;
    }

    gl.useProgram(program);

    const positionBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
    gl.bufferData(
      gl.ARRAY_BUFFER,
      new Float32Array([-1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, 1]),
      gl.STATIC_DRAW
    );

    const posAttr = gl.getAttribLocation(program, 'a_position');
    gl.enableVertexAttribArray(posAttr);
    gl.vertexAttribPointer(posAttr, 2, gl.FLOAT, false, 0, 0);

    const uTime = gl.getUniformLocation(program, 'u_time');
    const uRes = gl.getUniformLocation(program, 'u_resolution');
    const uMouse = gl.getUniformLocation(program, 'u_mouse');
    const uIntensity = gl.getUniformLocation(program, 'u_intensity');

    let mouse = { x: window.innerWidth / 2, y: window.innerHeight / 2 };

    const handleMouseMove = (e: MouseEvent) => {
      mouse.x = e.clientX;
      mouse.y = window.innerHeight - e.clientY;
    };

    const handleTouchMove = (e: TouchEvent) => {
      if (e.touches.length > 0) {
        mouse.x = e.touches[0].clientX;
        mouse.y = window.innerHeight - e.touches[0].clientY;
      }
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('touchmove', handleTouchMove, { passive: true });

    function syncSize() {
      if (!canvas) return;
      const dpr = Math.min(window.devicePixelRatio || 1, 2);
      const w = window.innerWidth;
      const h = window.innerHeight;
      if (canvas.width !== w * dpr || canvas.height !== h * dpr) {
        canvas.width = w * dpr;
        canvas.height = h * dpr;
      }
    }

    window.addEventListener('resize', syncSize);
    syncSize();

    let startTime = performance.now();

    function render(currentTime: number) {
      if (!gl || !canvas) return;
      syncSize();
      gl.viewport(0, 0, canvas.width, canvas.height);

      const elapsedSeconds = ((currentTime - startTime) * 0.001) * speed;

      if (uTime) gl.uniform1f(uTime, elapsedSeconds);
      if (uRes) gl.uniform2f(uRes, canvas.width, canvas.height);
      if (uMouse) gl.uniform2f(uMouse, mouse.x * (window.devicePixelRatio || 1), mouse.y * (window.devicePixelRatio || 1));
      if (uIntensity) gl.uniform1f(uIntensity, intensity);

      gl.drawArrays(gl.TRIANGLES, 0, 6);
      animationFrameId = requestAnimationFrame(render);
    }

    animationFrameId = requestAnimationFrame(render);

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('touchmove', handleTouchMove);
      window.removeEventListener('resize', syncSize);
      if (program && gl) {
        gl.deleteProgram(program);
        gl.deleteShader(vs);
        gl.deleteShader(fs);
      }
    };
  }, [theme, intensity, speed]);

  return (
    <canvas
      id="bg-canvas"
      ref={canvasRef}
      className="fixed inset-0 w-full h-full pointer-events-none -z-10 transition-opacity duration-700"
      style={{ display: 'block', width: '100vw', height: '100vh' }}
    />
  );
};
