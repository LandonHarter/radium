#version 330 core

struct Light {

    vec3 position;
    vec3 color;
    float intensity;
    float attenuation;

};

struct Material {

    float reflectivity;
    float shineDamper;

};

in vec3 vertex_position;
in vec2 vertex_textureCoord;
in vec3 vertex_normal;

in vec4 worldPosition;
in mat4 viewMatrix;
in vec4 lightSpaceVector;

out vec4 outColor;

uniform sampler2D tex;
uniform sampler2D depth;

uniform Light lights[1023];
uniform int lightCount;
uniform float ambient;

uniform bool useBlinn;

uniform Material material;

float CalculateShadow(int lightIndex) {
    vec3 projectionCoords = lightSpaceVector.xyz / lightSpaceVector.w;
    projectionCoords = projectionCoords * 0.5f + 0.5f;
    float closestDepth = texture(depth, projectionCoords.xy).r;
    float currentDepth = projectionCoords.z;

    vec3 toLightVector = lights[lightIndex].position - worldPosition.xyz;
    vec3 lightDirection = -normalize(toLightVector);
    float bias = max(0.05f * (1.0f - dot(vertex_normal, lightDirection)), 0.005f);

    float shadow = currentDepth - bias > closestDepth ? 1.0f : 0.0f;
    vec2 texelSize = 1.0 / textureSize(depth, 0);
    for(int x = -1; x <= 1; x++)
    {
        for(int y = -1; y <= 1; y++)
        {
            float pcfDepth = texture(depth, projectionCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
        }
    }
    shadow /= 9.0;

    if (projectionCoords.z > 1.0f) {
        shadow = 0.0f;
    }

    return shadow;
}

vec4 CalculateLight() {
    vec3 finalLight = vec3(0.0f);
    for (int i = 0; i < lightCount; i++) {
        vec3 toLightVector = lights[i].position - worldPosition.xyz;
        vec3 toCameraVector = (inverse(viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition.xyz;
        vec3 unitNormal = normalize(vertex_normal);
        vec3 unitLightVector = normalize(toLightVector);
        vec3 unitCameraVector = normalize(toCameraVector);
        vec3 lightDirection = -unitLightVector;
        vec3 halfwayDirection = normalize(unitLightVector + unitCameraVector);

        vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
        float specularFactor = dot(useBlinn ? halfwayDirection : reflectedLightDirection, unitCameraVector);
        specularFactor = max(specularFactor, 0.0f);
        float dampedFactor = pow(specularFactor, material.shineDamper);
        vec3 specular = dampedFactor * material.reflectivity * lights[i].color;

        float nDotl = dot(unitNormal, unitLightVector);
        float brightness = max(nDotl, 0.0);
        vec3 diffuse = brightness * lights[i].color;

        float distanceFromLight = length(lights[i].position - vertex_position);
        float attenuation = 1.f / (1.f + lights[i].attenuation * distanceFromLight * 0.0075f * (distanceFromLight * distanceFromLight));

        diffuse *= attenuation;
        specular *= attenuation;

        finalLight += (ambient + (1.0f - CalculateShadow(i)) * ((diffuse + specular))) * lights[i].color * lights[i].intensity * attenuation;
    }

    return vec4(max(finalLight, ambient), 1.0f);
}

void main() {
    float depthValue = texture(depth, vertex_textureCoord).r;
    outColor = texture(tex, vertex_textureCoord) * CalculateLight();
}