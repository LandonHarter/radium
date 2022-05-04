package Radium.Components.Graphics;

import Integration.Project.AssetsListener;
import Integration.Project.ProjectFiles;
import Radium.Color;
import Radium.Component;
import Radium.Graphics.Lighting.LightType;
import Radium.Graphics.RenderQueue;
import Radium.Graphics.RendererType;
import Radium.Graphics.Renderers.CustomRenderer;
import Radium.Graphics.Renderers.Renderer;
import Radium.Graphics.Renderers.Renderers;
import Radium.Graphics.Shader.Shader;
import Radium.Graphics.Shader.ShaderLibrary;
import Radium.Graphics.Shader.ShaderUniform;
import Radium.Graphics.Shader.Type.ShaderLight;
import Radium.Graphics.Shader.Type.ShaderMaterial;
import Radium.Graphics.Texture;
import Radium.Math.Vector.Vector2;
import Radium.Math.Vector.Vector3;
import Radium.PerformanceImpact;
import Radium.PostProcessing.UniformType;
import Radium.System.FileExplorer;
import Radium.System.Popup;
import Radium.Util.FileUtility;
import RadiumEditor.Annotations.HideInEditor;
import RadiumEditor.Annotations.RunInEditMode;
import RadiumEditor.Console;
import RadiumEditor.EditorGUI;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Updates and renders the mesh
 */
@RunInEditMode
public class MeshRenderer extends Component implements AssetsListener {

    public transient Renderer renderer;
    /**
     * The rendering system to use
     */
    public RendererType renderType = RendererType.Lit;
    private transient int PreviousRenderType = 1;
    /**
     * If enabled, will cull back faces of object
     */
    public boolean cullFaces = false;
    public boolean transparent = false;

    private static String defaultShader = "#version 330 core\n\nout vec4 fragColor;\n\nuniform sampler2D MainTex;\n\nvoid main() {\n   fragColor = texture(MainTex, uv);\n}";
    @HideInEditor
    public File shaderPath;
    @HideInEditor
    public String shader;

    public Shader s;

    private transient ProjectFiles assets;

    /**
     * Create empty mesh renderer with default rendering settings
     */
    public MeshRenderer() {
        icon = new Texture("EngineAssets/Editor/Icons/meshrenderer.png").textureID;
        renderer = Renderers.renderers.get(renderType.ordinal());

        name = "Mesh Renderer";
        description = "Renders mesh data held in MeshFilter component";
        impact = PerformanceImpact.Low;
        submenu = "Graphics";

        lightTexture = new Texture("EngineAssets/Editor/Icons/light.png").textureID;
    }

    
    public void Start() {

    }

    
    public void Update() {
        if (transparent) {
            RenderQueue.transparent.add(this);
        } else {
            RenderQueue.opaque.add(this);
        }
    }

    public void Render() {
        if (cullFaces) GL11.glEnable(GL11.GL_CULL_FACE);
        renderer.Render(gameObject);
        GL11.glDisable(GL11.GL_CULL_FACE);
    }
    
    public void Stop() {

    }

    
    public void OnAdd() {
        assets = new ProjectFiles();
        assets.RegisterListener(this);
        if (renderType == RendererType.Custom) {
            assets.Initialize(new File(shader).getParent());
        }

        PreviousRenderType = renderType.ordinal();
    }
    
    public void OnRemove() {
        if (gameObject.ContainsComponent(Outline.class)) {
            Console.Error("Outline depends on Mesh Renderer");
            gameObject.RemoveComponent(Outline.class);
        }
    }
    
    public void UpdateVariable() {
        if (renderType.ordinal() != PreviousRenderType) {
            if (renderType == RendererType.Custom) {
                boolean create = Popup.YesNo("Would you like to create a new file?");
                String path;
                if (create) path = FileExplorer.Create("glsl");
                else path = FileExplorer.Choose("glsl");
                if (path != null && !path.isEmpty()) {
                    if (create) {
                        FileUtility.Create(path);
                        FileUtility.Write(new File(path), defaultShader);
                    }

                    if (Files.exists(Paths.get(path)) && FileUtility.IsFileType(new File(path), new String[] { "glsl" })) {
                        assets.Destroy();
                        assets.Initialize(new File(path).getParent());

                        CreateRenderer(path);
                    }
                }
            } else {
                renderer = Renderers.renderers.get(renderType.ordinal());
                if (gameObject.ContainsComponent(Outline.class)) {
                    gameObject.GetComponent(Outline.class).shader = Renderers.GetRenderer(renderType).shader;
                }
            }

            PreviousRenderType = renderType.ordinal();
        }
    }

    public void CreateRenderer(String path) {
        List<ShaderUniform> previousUniforms = renderer.shader.uniforms;
        renderer = new CustomRenderer();
        renderer.shader = new Shader("EngineAssets/Shaders/basicvert.glsl", path, false);
        renderer.shader.uniforms = previousUniforms;
        CreateLibraries(renderer.shader);
        renderer.shader.Compile();

        shaderPath = new File(path);
        shader = path;
        s = renderer.shader;
    }

    public void CreateRenderer(String path, List<ShaderUniform> previousUniforms) {
        renderer = new CustomRenderer();
        renderer.shader = new Shader("EngineAssets/Shaders/basicvert.glsl", path, false);
        renderer.shader.uniforms = previousUniforms;
        CreateLibraries(renderer.shader);
        renderer.shader.Compile();

        for (ShaderUniform uniform : previousUniforms) {
            if (uniform.shader == null) {
                uniform.shader = renderer.shader;
            }
        }

        shaderPath = new File(path);
        shader = path;
        s = renderer.shader;
    }

    private void CreateLibraries(Shader shader) {
        shader.AddLibrary(new ShaderLibrary("EngineAssets/Shaders/Libraries/include.glsl"), false);
        shader.AddLibrary(new ShaderLibrary("EngineAssets/Shaders/Libraries/math.glsl"), false);
        shader.AddLibrary(new ShaderLibrary("EngineAssets/Shaders/Libraries/noise.glsl"), false);
        shader.AddLibrary(new ShaderLibrary("EngineAssets/Shaders/Libraries/util.glsl"), false);
        shader.AddLibrary(new ShaderLibrary("EngineAssets/Shaders/Libraries/lighting.glsl"), false);
        shader.AddLibrary(new ShaderLibrary("EngineAssets/Shaders/Libraries/color.glsl"), false);
        shader.AddLibrary(new ShaderLibrary("EngineAssets/Shaders/Libraries/texture.glsl"), false);
    }

    public void GUIRender() {
        if (renderType == RendererType.Custom && shaderPath != null) {
            if (ImGui.button("Compile Shader")) {
                CreateRenderer(shader);
            }
            ImGui.sameLine();
            if (ImGui.treeNodeEx(shaderPath.getName(), ImGuiTreeNodeFlags.SpanAvailWidth)) {
                for (ShaderUniform uniform : renderer.shader.GetUniforms()) {
                    RenderUniform(uniform);
                }

                ImGui.treePop();
            }
        }
    }

    private int lightTexture;
    private void RenderUniform(ShaderUniform uniform) {
        if (uniform.type == Integer.class) {
            uniform.value = EditorGUI.DragInt(uniform.name, (int)uniform.value);
        } else if (uniform.type == Float.class) {
            uniform.value = EditorGUI.DragFloat(uniform.name, (float)uniform.value);
        } else if (uniform.type == Boolean.class) {
            uniform.value = EditorGUI.Checkbox(uniform.name, (boolean)uniform.value);
        } else if (uniform.type == Vector2.class) {
            uniform.value = EditorGUI.DragVector2(uniform.name, (Vector2)uniform.value);
        } else if (uniform.type == Vector3.class) {
            uniform.value = EditorGUI.DragVector3(uniform.name, (Vector3) uniform.value);
        } else if (uniform.type == Texture.class) {
            File value = (uniform.value == null) ? null : ((Texture)uniform.value).file;
            File f = EditorGUI.FileReceive(new String[] { "png", "jpg", "bpm" }, "Texture", value);
            ImGui.sameLine();
            ImGui.text(uniform.name);
            if (f != null) {
                uniform.value = new Texture(f.getAbsolutePath());
            }
        } else if (uniform.type == ShaderMaterial.class) {
            if (uniform.value == null) uniform.value = new ShaderMaterial();
            ShaderMaterial mat = (ShaderMaterial)uniform.value;
            if (ImGui.collapsingHeader(uniform.name, ImGuiTreeNodeFlags.SpanAvailWidth)) {
                ImGui.indent();
                mat.shineDamper = EditorGUI.DragFloat("Shine Damper", mat.shineDamper);
                mat.reflectivity = EditorGUI.DragFloat("Reflectivity", mat.reflectivity);
                ImGui.unindent();
            }
        } else if (uniform.type == ShaderLight.class) {
            if (uniform.value == null) uniform.value = new ShaderLight();
            ShaderLight light = (ShaderLight)uniform.value;
            ImGui.image(lightTexture, 20, 20);
            ImGui.sameLine();
            if (ImGui.collapsingHeader(uniform.name, ImGuiTreeNodeFlags.SpanAvailWidth)) {
                ImGui.indent();
                light.position = EditorGUI.DragVector3("Position", light.position);
                light.color = EditorGUI.ColorField("Color", light.color);
                light.intensity = EditorGUI.DragFloat("Intensity", light.intensity);
                light.attenuation = EditorGUI.DragFloat("Attenuation", light.attenuation);
                light.lightType = (LightType)EditorGUI.EnumSelect("Light Type", light.lightType.ordinal(), LightType.class);
                ImGui.unindent();
            }
        } else if (uniform.type == Color.class) {
            if (uniform.value == null) uniform.value = new Color(255, 255, 255, 255);
            uniform.value = EditorGUI.ColorField(uniform.name, (Color)uniform.value);
        }
    }

    @Override
    public void OnFileCreated(File file) {

    }

    @Override
    public void OnFileDeleted(File file) {

    }

    @Override
    public void OnFileChanged(File file) {
        if (file.getName().equals(shaderPath.getName())) {
            CreateRenderer(shader);
        }
    }

}