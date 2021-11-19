package Engine.Components.Graphics;

import Editor.Console;
import Engine.Component;
import Engine.Graphics.RendererType;
import Engine.Graphics.Renderers.Renderer;
import Engine.Graphics.Renderers.Renderers;
import Engine.Graphics.Texture;
import Engine.PerformanceImpact;
import imgui.ImGui;
import org.lwjgl.opengl.GL11;

public class MeshRenderer extends Component {

    private transient Renderer renderer;
    public RendererType renderType = RendererType.Lit;
    public boolean cullFaces = true;

    public MeshRenderer() {
        icon = new Texture("EngineAssets/Editor/Icons/meshrenderer.png").textureID;
        renderer = Renderers.renderers.get(renderType.ordinal());

        RunInEditMode = true;
        description = "Renders mesh data held in MeshFilter component";
        impact = PerformanceImpact.Low;
        submenu = "Graphics";
    }

    @Override
    public void Start() {

    }

    @Override
    public void Update() {
        if (cullFaces) GL11.glEnable(GL11.GL_CULL_FACE);
        renderer.Render(gameObject);
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    @Override
    public void Stop() {

    }

    @Override
    public void OnAdd() {

    }

    @Override
    public void OnRemove() {

    }

    @Override
    public void UpdateVariable() {
        renderer = Renderers.renderers.get(renderType.ordinal());
    }

    @Override
    public void GUIRender() {

    }

}