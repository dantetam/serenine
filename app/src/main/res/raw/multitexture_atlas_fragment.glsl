precision mediump float;       	// Set the default precision to medium. We don't need as high of a 
								// precision in the fragment shader.
uniform vec3 u_LightPos;       	// The position of the light in eye space.
uniform vec3 u_CameraPos;       // The position of the camera in eye space

uniform sampler2D blackTexture;    // The input texture.
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;
  
varying vec3 v_Position;		// Interpolated position for this fragment.
varying vec3 v_Normal;         	// Interpolated normal for this fragment.
varying vec2 v_TexCoordinate0;   // Interpolated texture coordinate per fragment.
varying vec2 v_TexCoordinate1;
varying vec2 v_TexCoordinate2;
varying vec2 v_TexCoordinate3;

//varying vec4 v_Color;
  
// The entry point for our fragment shader.
void main()                    		
{
    vec4 blendMapColor = texture2D(blendMap, v_TexCoordinate0);
    float blackTextureShade = 1.0 - (blendMapColor.r + blendMapColor.g + blendMapColor.b);
    //vec2 tiled = v_TexCoordinate * 40;
    vec4 blackTextureColor = texture2D(blackTexture, v_TexCoordinate0) * blackTextureShade;
    vec4 rTextureColor = texture2D(rTexture, v_TexCoordinate1) * blendMapColor.r;
    vec4 gTextureColor = texture2D(gTexture, v_TexCoordinate2) * blendMapColor.g;
    vec4 bTextureColor = texture2D(bTexture, v_TexCoordinate3) * blendMapColor.b;

    vec4 texel = blackTextureColor + rTextureColor + gTextureColor + bTextureColor;
    //texel.a = 1.0;

    //texel = vec4(1.0, 0.0, 0.0, 1.0);

    //vec4 texel = texture2D(u_Texture, v_TexCoordinate);
    if (texel.a < 0.5)
        discard;
	// Will be used for attenuation.
    float distance = length(u_LightPos - v_Position);                  
	
	// Get a lighting direction vector from the light to the vertex.
    vec3 lightVector = normalize(u_LightPos - v_Position);

	// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
	// pointing in the same direction then it will get max illumination.
    float diffuse = max(dot(v_Normal, lightVector), 0.3);

	vec3 normalDirection = normalize(v_Normal);
    vec3 viewDirection = normalize(u_CameraPos - v_Position);

	// Add attenuation. 
    //diffuse = diffuse * (1.0 / distance);
    
    // Add ambient lighting
    diffuse = diffuse + 0.3;

	// Multiply the color by the diffuse illumination level and texture value to get final output color.
    //gl_FragColor = texture2D(u_Texture, v_TexCoordinate);

    //gl_FragColor = vec4(diffuse, diffuse, diffuse, 1.0);
    //gl_FragColor = mix(diffuse, gray, 0.5);

    gl_FragColor = (diffuse * texel);

    //vec4 colored = (diffuse * texture2D(u_Texture, v_TexCoordinate));
    //float newOpacity = min(1.0, colored.w / abs(dot(viewDirection, normalDirection)));
    //gl_FragColor = vec4(vec3(colored), newOpacity);
}

