package RadiumEditor;

import Integration.Project.Project;
import Radium.EventSystem.EventSystem;
import Radium.EventSystem.Events.Event;
import Radium.EventSystem.Events.EventType;
import Radium.Graphics.Texture;
import Radium.Input.Keys;
import Radium.SceneManagement.Scene;
import Radium.SceneManagement.SceneManager;
import Radium.System.FileExplorer;
import Radium.Util.ThreadUtility;
import Radium.Window;
import RadiumEditor.ImNotify.ImNotify;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import org.newdawn.slick.util.Log;

import java.io.*;
import java.security.Key;

/**
 * Window menu bar
 */
public class MenuBar {

    private static int Play, Stop;
    private static int Logo;
    private static int Minimize, Maximize, Close;

    protected MenuBar() {}

    /**
     * Initialize textures and keybinds
     */
    public static void Initialize() {
        Play = new Texture("EngineAssets/Editor/menubarplay.png").textureID;
        Stop = new Texture("EngineAssets/Editor/menubarstop.png").textureID;
        Logo = new Texture("EngineAssets/Textures/Icon/icon.png").textureID;

        Minimize = new Texture("EngineAssets/Editor/Window/minimize.png").textureID;
        Maximize = new Texture("EngineAssets/Editor/Window/maximize.png").textureID;
        Close = new Texture("EngineAssets/Editor/Window/close.png").textureID;

        KeyBindManager.RegisterKeybind(new Keys[] { Keys.LeftCtrl, Keys.O }, () -> {
            OpenScene();
        });
        KeyBindManager.RegisterKeybind(new Keys[] { Keys.LeftCtrl, Keys.N }, () -> {
            NewScene();
        });
        KeyBindManager.RegisterKeybind(new Keys[] { Keys.F5 }, () -> {
            EventSystem.Trigger(null, new Event(EventType.Play));
        });
        KeyBindManager.RegisterKeybind(new Keys[] { Keys.F6 }, () -> {
            EventSystem.Trigger(null, new Event(EventType.Stop));
        });
    }

    /**
     * Render the menu bar
     */
    public static void RenderMenuBar() {
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0f, 12f);
        if (ImGui.beginMainMenuBar()) {
            ImGui.image(Logo, 42.5f, 42.5f);

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 10f, 42.5f);
            if (ImGui.beginMenu("File")) {
                ImGui.popStyleVar();
                if (ImGui.menuItem("New Scene", "CTRL+N")) {
                    NewScene();
                }

                if (ImGui.menuItem("Open Scene", "CTRL+O")) {
                    OpenScene();
                }

                if (ImGui.menuItem("Save Scene", "CTRL+S")) {
                    SceneManager.GetCurrentScene().Save();
                }

                if (ImGui.menuItem("Open VSCode")) {
                    ThreadUtility.Run(() -> {
                        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "code", Project.Current().assets);
                        builder.redirectErrorStream(true);
                        try {
                            Process p = builder.start();
                            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            String line;
                            while (true) {
                                line = r.readLine();
                                if (line == null) {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            Console.Error(e);
                        }
                    });
                }

                ImGui.separator();

                if (ImGui.menuItem("Exit")) {
                    Window.Close();
                }

                ImGui.endMenu();
            } else {
                ImGui.popStyleVar();
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 10f, 42.5f);
            if (ImGui.beginMenu("Edit")) {
                ImGui.popStyleVar();
                if (ImGui.menuItem("Preferences")) {
                    Preferences.Show();
                }

                ImGui.endMenu();
            } else {
                ImGui.popStyleVar();
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 10f, 42.5f);
            if (ImGui.beginMenu("Run")) {
                ImGui.popStyleVar();
                RenderPlayStop();

                ImGui.endMenu();
            } else {
                ImGui.popStyleVar();
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 10f, 42.5f);
            if (ImGui.beginMenu("Editor Windows")) {
                ImGui.popStyleVar();
                for (EditorWindow window : Editor.GetAllEditorWindows()) {
                    if (ImGui.menuItem(window.MenuName)) {
                        window.Start();
                        window.Render = true;
                    }
                }

                if (ImGui.menuItem("Node Scripting")) {
                    NodeScripting.Render = true;
                }

                ImGui.endMenu();
            } else {
                ImGui.popStyleVar();
            }

            RenderWindowControls();
            ImGui.endMainMenuBar();
        }
        ImGui.popStyleVar();
    }

    private static void NewScene() {
        String newScenePath = FileExplorer.Create("radium");
        if (newScenePath == null || newScenePath.isEmpty()) {
            return;
        }

        File file = new File(newScenePath);
        try {
            if (!file.createNewFile()) {
                return;
            }

            FileWriter writer = new FileWriter(file);
            writer.write("[]");
            writer.flush();
            writer.close();

            SceneManager.SwitchScene(new Scene(file.getPath()));
        } catch (Exception e) {
            Console.Error(e);
        }
    }

    private static void OpenScene() {
        String openScene = FileExplorer.Choose("radium");

        if (openScene != null) {
            SceneManager.SwitchScene(new Scene(openScene));
        }
    }

    private static void RenderPlayStop() {
        ImGui.image(Play, 17, 17);
        ImGui.sameLine();
        if (ImGui.menuItem("Play", "F5")) {
            EventSystem.Trigger(null, new Event(EventType.Play));
        }

        ImGui.image(Stop, 17, 17);
        ImGui.sameLine();
        if (ImGui.menuItem("Stop", "F6")) {
            EventSystem.Trigger(null, new Event(EventType.Stop));
        }
    }

    private static void RenderWindowControls() {
        ImVec4 menuBar = ImGui.getStyle().getColor(ImGuiCol.MenuBarBg);

        ImGui.setCursorPosX(ImGui.getWindowWidth() - 135.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0f, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemInnerSpacing, 0f, 0f);
        ImGui.pushStyleColor(ImGuiCol.Button, menuBar.x, menuBar.y, menuBar.z, menuBar.w);

        if (ImGui.imageButton(Minimize, 45f, 25f)) {
            Window.Minimize();
        }
        if (ImGui.imageButton(Maximize, 45f, 25f)) {
            Window.Maximize();
        }

        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 0.0f, 0.0f, 1.0f);
        if (ImGui.imageButton(Close, 45f, 25f)) {
            Window.Close();
        }

        ImGui.popStyleColor(2);
        ImGui.popStyleVar(2);
    }

}
