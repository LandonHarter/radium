package Radium.Components;

import Radium.Component;
import Radium.EventSystem.EventSystem;
import Radium.EventSystem.Events.Event;
import Radium.EventSystem.Events.EventType;
import Radium.Scripting.NodeAction;
import Radium.Scripting.NodeScript;
import Radium.Scripting.Nodes;
import Radium.Scripting.ScriptingNode;
import Radium.System.FileExplorer;
import RadiumEditor.Annotations.RunInEditMode;
import RadiumEditor.Console;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

import java.util.ArrayList;
import java.util.List;

public class NodeScriptManager extends Component {

    private transient List<NodeScript> scripts = new ArrayList<>();
    private List<String> scriptPaths = new ArrayList<>();

    private float buttonPadding = 20;

    public NodeScriptManager() {
        name = "Script Manager";
        submenu = "Scripting";
    }

    @Override
    public void Start() {
        for (NodeScript script : scripts) {
            for (ScriptingNode node : script.nodes) {
                node.action = NodeAction.ActionFromType(node);
                node.start = NodeAction.StartFromType(node);
                node.update = NodeAction.UpdateFromType(node);
                node.display = NodeAction.DisplayFromType(node);
            }

            script.Start();
        }
    }

    @Override
    public void Update() {
        for (NodeScript script : scripts) {
            script.Update();
        }
    }

    @Override
    public void OnAdd() {
        ReloadScripts();
    }

    @Override
    public void GUIRender() {
        if (ImGui.button("Refresh")) {
            ReloadScripts();
        }
        ImGui.sameLine();
        if (ImGui.treeNodeEx("Scripts", ImGuiTreeNodeFlags.SpanAvailWidth)) {
            for (int i = 0; i < scripts.size(); i++) {
                NodeScript script = scripts.get(i);

                ImGui.treeNodeEx(script.name, ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.Leaf);
                ImGui.treePop();

                ImGui.sameLine();
                if (ImGui.button("Remove")) {
                    scripts.remove(i);
                }
            }

            ImGui.treePop();
        }

        ImGui.setCursorPosX(buttonPadding);
        if (ImGui.button("Add Script", ImGui.getWindowWidth() - (buttonPadding * 2), 25)) {
            String path = FileExplorer.Choose("script");
            if (path != null) {
                LoadScript(path);
            }
        }
    }

    private void ReloadScripts() {
        scripts.clear();

        int pathSize = scriptPaths.size();
        for (int i = 0; i < pathSize; i++) {
            String value = scriptPaths.get(i);
            scriptPaths.remove(i);

            LoadScript(value);
        }
    }

    private void LoadScript(String path) {
        NodeScript script = NodeScript.Load(path);
        script.gameObject = gameObject;

        for (ScriptingNode node : script.nodes) {
            node.gameObject = gameObject;
        }

        scripts.add(script);
        scriptPaths.add(path);

        Nodes.NodePlay = true;
        EventSystem.Trigger(null, new Event(EventType.Play));
        EventSystem.Trigger(null, new Event(EventType.Stop));
        Console.Clear(false);
        Nodes.NodePlay = false;
    }

}
