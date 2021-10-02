package Engine.Components.Rendering;

import Engine.Color;
import Engine.Component;
import Engine.Graphics.Shader;
import Engine.Graphics.Texture;
import Engine.Variables;

public class Light extends Component {

    public static int LightIndex = 0;
    private int index;

    private Shader shader;

    public Color color = new Color(255, 255, 255);
    public float intensity = 1f;
    public float attenuation = 0.045f;

    public Light() {
        icon = new Texture("EngineAssets/Editor/Icons/light.png").textureID;
    }

    @Override
    public void Start() {
        index = LightIndex;
        LightIndex++;

        shader = Variables.LitRenderer.shader;
    }

    @Override
    public void Update() {
        UpdateUniforms();
    }

    @Override
    public void GUIRender() {

    }

    private void UpdateUniforms() {
        shader.Bind();

        shader.SetUniform("lights[" + index + "].position", gameObject.transform.position);
        shader.SetUniform("lights[" + index + "].color", color.ToVector3());
        shader.SetUniform("lights[" + index + "].intensity", intensity);
        shader.SetUniform("lights[" + index + "].attenuation", attenuation);

        shader.Unbind();
    }

}
