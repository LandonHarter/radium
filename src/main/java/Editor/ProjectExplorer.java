package Editor;

import Engine.Color;
import Engine.Graphics.Texture;
import Engine.Input.Input;
import Engine.SceneManagement.Scene;
import Engine.SceneManagement.SceneManager;
import Engine.Util.FileUtils;
import Engine.Util.NonInstantiatable;
import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class ProjectExplorer extends NonInstantiatable {

    private static File currentDirectory = new File("Assets/");
    private static List<File> filesInCurrentDirectory = new ArrayList<>();
    public static File SelectedFile;

    private static int File, Folder, BackArrow;
    private static Hashtable<String, Integer> FileIcons = new Hashtable<>();
    private static Hashtable<String, Consumer<File>> FileActions = new Hashtable<>();
    public static Hashtable<String, Consumer<File>> FileGUIRender = new Hashtable<>();

    private static Color SelectedColor = new Color(80 / 255f, 120 / 255f, 237 / 255f);

    private static int SelectedImage = 0;

    public static void Initialize() {
        File = new Texture("EngineAssets/Editor/Explorer/file.png").textureID;
        Folder = new Texture("EngineAssets/Editor/Explorer/folder.png").textureID;
        BackArrow = new Texture("EngineAssets/Editor/Explorer/backarrow.png").textureID;
        RegisterExtensions();
        RegisterActions();
        RegisterFileGUI();

        UpdateDirectory();
    }

    public static void Render() {
        ImGui.begin("Project Explorer", ImGuiWindowFlags.MenuBar);

        ImGui.beginMenuBar();

        if (ImGui.imageButton(BackArrow, 20, 17)) {
            String[] back = currentDirectory.getPath().split(Pattern.quote("\\"));
            if (back.length > 1) {
                List<String> mback = new ArrayList<>();

                for (int i = 0; i < back.length - 1; i++) {
                    mback.add(back[i]);
                }

                String finalPath = "";
                for (String p : mback) {
                    finalPath += p + "/";
                }

                currentDirectory = new File(finalPath);
                UpdateDirectory();
            }
        }

        if (ImGui.button("Home")) {
            currentDirectory = new File("Assets/");
            UpdateDirectory();
        }
        if (ImGui.button("Reload")) {
            UpdateDirectory();
        }

        String path = currentDirectory.getPath().replace("\\", " > ");
        ImGui.text(path);

        ImGui.endMenuBar();

        if (SelectedFile == null) {
            SelectedImage = 0;
        }

        int index = 1;
        for (int i = 0; i < filesInCurrentDirectory.size(); i++) {
            File file = filesInCurrentDirectory.get(i);

            if (file == SelectedFile) {
                ImGui.pushStyleColor(ImGuiCol.FrameBg, ImColor.floatToColor(SelectedColor.r, SelectedColor.g, SelectedColor.b));
            }

            if (ImGui.beginChildFrame(index, 100, 110)) {
                int icon = file.isFile() ? GetIcon(file) : Folder;
                if (icon == 0) icon = File;

                ImGui.image(icon, 90, 80);
                ImGui.text(file.getName());

                if (file == SelectedFile) {
                    ImGui.popStyleColor();
                }

                ImGui.endChildFrame();
                ImGui.sameLine();

                if (Input.GetMouseButton(0) && ImGui.isItemHovered()) {
                    if (file.isDirectory()) {
                        currentDirectory = file;
                        UpdateDirectory();
                    } else {
                        String extension = FileUtils.GetFileExtension(file);
                        FileActions.getOrDefault(extension, (File) -> {}).accept(file);

                        SelectedFile = file;

                        if (extension.equals("png") || extension.equals("jpg") || extension.equals("bmp")) {
                            SelectedImage = new Texture(SelectedFile.getPath()).textureID;
                        }

                        SceneHierarchy.current = null;
                    }
                }
            }

            index++;
        }

        ImGui.end();
    }

    private static void UpdateDirectory() {
        if (!currentDirectory.isDirectory()) return;
        filesInCurrentDirectory.clear();
        SelectedFile = null;

        for (File file : currentDirectory.listFiles()) {
            filesInCurrentDirectory.add(file);
        }
    }

    private static int GetIcon(File file) {
        return FileIcons.getOrDefault(file.getName().split("[.]")[1], 0);
    }

    private static void RegisterExtensions() {
        FileIcons.put("java", LoadTexture("EngineAssets/Editor/Explorer/java.png"));
        FileIcons.put("glsl", LoadTexture("EngineAssets/Editor/Explorer/shader.png"));

        FileIcons.put("fbx", LoadTexture("EngineAssets/Editor/Explorer/model.png"));
        FileIcons.put("obj", LoadTexture("EngineAssets/Editor/Explorer/model.png"));

        FileIcons.put("radiumscene", LoadTexture("EngineAssets/Textures/iconwhite.png"));

        FileIcons.put("ttf", LoadTexture("EngineAssets/Editor/Explorer/font.png"));

        FileIcons.put("png", LoadTexture("EngineAssets/Editor/Explorer/picture.png"));
        FileIcons.put("jpg", LoadTexture("EngineAssets/Editor/Explorer/picture.png"));
        FileIcons.put("bmp", LoadTexture("EngineAssets/Editor/Explorer/picture.png"));

        FileIcons.put("mp3", LoadTexture("EngineAssets/Editor/Explorer/audio.png"));
        FileIcons.put("ogg", LoadTexture("EngineAssets/Editor/Explorer/audio.png"));
        FileIcons.put("wav", LoadTexture("EngineAssets/Editor/Explorer/audio.png"));
    }

    private static void RegisterActions() {
        FileActions.put("radiumscene", (File file) -> {
            SceneManager.SwitchScene(new Scene(file.getPath()));
        });
    }

    private static void RegisterFileGUI() {
        FileGUIRender.put("png", (File file) -> {
            ImGui.beginChildFrame(1, 300, 300);
            ImGui.image(SelectedImage, 300, 290);
            ImGui.endChildFrame();
        });
        FileGUIRender.put("jpg", (File file) -> {
            ImGui.beginChildFrame(1, 300, 300);
            ImGui.image(SelectedImage, 300, 290);
            ImGui.endChildFrame();
        });
        FileGUIRender.put("bmp", (File file) -> {
            ImGui.beginChildFrame(1, 300, 300);
            ImGui.image(SelectedImage, 300, 290);
            ImGui.endChildFrame();
        });
    }

    public static void BasicFileReader(File file) {
        ImGui.beginChildFrame(1, 500, 900);
        ImGui.text(FileUtils.ReadFile(file));
        ImGui.endChildFrame();
    }

    private static int LoadTexture(String path) {
        return new Texture(path).textureID;
    }

}