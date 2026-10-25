#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform float Pulse;    // 0 between the beats, 1 on a full beat
uniform float Progress; // 0 at the start of the countdown, 1 at the end

out vec4 fragColor;

void main() {
    vec2 centered = texCoord - 0.5;
    float dist = length(centered);

    float squeeze = Progress * 0.8 - 0.2;
    // on a beat the picture is squeezed towards the centre, the most at the edges
    vec2 uv = 0.5 + centered * (1.0 - squeeze * Pulse * smoothstep(0.1, 0.7, dist));

    // the colour channels drift apart radially
    float aberration = Progress * 0.1;
    vec2 offset = centered * (aberration * Pulse);
    vec3 color = vec3(
        texture(DiffuseSampler, uv + offset).r,
        texture(DiffuseSampler, uv).g,
        texture(DiffuseSampler, uv - offset).b
    );

    // the colour drains out of the world while the countdown runs
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    color = mix(color, vec3(luma), 0.95 * Progress);

    // a hint of red around the edges, only on the beat
    float edge = smoothstep(0.25, 0.85, dist);
    color = mix(color, color * vec3(1.0, 0.3, 0.3), edge * 0.45 * Pulse);

    fragColor = vec4(color, 1.0);
}
