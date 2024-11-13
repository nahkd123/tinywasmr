package tinywasmr.w4;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import tinywasmr.dbg.DebugStepMode;
import tinywasmr.dbg.gui.AboutTinyWasmR;
import tinywasmr.dbg.gui.CodeViewer;
import tinywasmr.dbg.gui.MachineController;
import tinywasmr.dbg.gui.MachineInspector;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.parser.binary.BinaryModuleParser;
import tinywasmr.w4.gui.KeyCombination;
import tinywasmr.w4.gui.KeybindEntry;
import tinywasmr.w4.gui.KeybindGroup;
import tinywasmr.w4.gui.W4Keybinds;
import tinywasmr.w4.gui.W4Screen;

public class Main extends Application {
	// GUI
	private ImFont font;
	private W4Screen screen;
	private W4Keybinds keybinds;
	private boolean showScreen = true;
	private boolean showKeybinds = false;

	private W4 console;

	// Debugger
	private MachineInspector inspector;
	private CodeViewer codeViewer;
	private MachineController controller;
	private boolean showInspector = false;
	private boolean showCodeViewer = false;
	private boolean showController = false;
	private KeybindEntry keybindPause;
	private KeybindEntry keybindStepIn;
	private KeybindEntry keybindStepNext;
	private KeybindEntry keybindStepOut;

	// Other
	private boolean showAboutTinyWasmR = false;
	private boolean showAboutImGui = false;

	public static void main(String[] args) {
		launch(new Main());
	}

	public Main() {}

	@Override
	protected void configure(Configuration config) {
		config.setTitle("TinyWasmR WASM-4 Fantasy Console");
	}

	@Override
	protected void initImGui(Configuration config) {
		super.initImGui(config);
		ImGuiIO io = ImGui.getIO();
		io.setIniFilename("imgui.ini");
		io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
		io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
		io.getFonts().addFontFromFileTTF("C:\\Windows\\Fonts\\consola.ttf", 16f);

		screen = new W4Screen();
		keybinds = new W4Keybinds(List.of(
			KeybindGroup.ofGamepad(0, (flag, state) -> {
				if (console != null) console.getInput().setGamepad(0, flag, state);
			}, Map.of(
				W4Input.BUTTON_UP, List.of(new KeyCombination(GLFW.GLFW_KEY_UP)),
				W4Input.BUTTON_DOWN, List.of(new KeyCombination(GLFW.GLFW_KEY_DOWN)),
				W4Input.BUTTON_LEFT, List.of(new KeyCombination(GLFW.GLFW_KEY_LEFT)),
				W4Input.BUTTON_RIGHT, List.of(new KeyCombination(GLFW.GLFW_KEY_RIGHT)),
				W4Input.BUTTON_1, List.of(new KeyCombination(GLFW.GLFW_KEY_X)),
				W4Input.BUTTON_2, List.of(new KeyCombination(GLFW.GLFW_KEY_Z)))),
			KeybindGroup.ofGamepad(1, (flag, state) -> {
				if (console != null) console.getInput().setGamepad(1, flag, state);
			}, Map.of(
				W4Input.BUTTON_UP, List.of(new KeyCombination(GLFW.GLFW_KEY_W)),
				W4Input.BUTTON_DOWN, List.of(new KeyCombination(GLFW.GLFW_KEY_S)),
				W4Input.BUTTON_LEFT, List.of(new KeyCombination(GLFW.GLFW_KEY_A)),
				W4Input.BUTTON_RIGHT, List.of(new KeyCombination(GLFW.GLFW_KEY_D)),
				W4Input.BUTTON_1, List.of(new KeyCombination(GLFW.GLFW_KEY_TAB)),
				W4Input.BUTTON_2, List.of(new KeyCombination(GLFW.GLFW_KEY_Q)))),
			KeybindGroup.ofGamepad(2, (flag, state) -> {
				if (console != null) console.getInput().setGamepad(2, flag, state);
			}, Map.of()),
			KeybindGroup.ofGamepad(3, (flag, state) -> {
				if (console != null) console.getInput().setGamepad(3, flag, state);
			}, Map.of()),
			new KeybindGroup("Debugger", List.of(
				keybindPause = new KeybindEntry("Pause/Resume Execution", action -> {
					if (action != GLFW.GLFW_PRESS) return;
					if (console == null) return;
					if (console.isRunning()) console.pause();
					else console.resume();
				}, new KeyCombination(GLFW.GLFW_KEY_PAUSE)),
				keybindStepIn = new KeybindEntry("Step Into", action -> {
					if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_REPEAT) return;
					if (console == null) return;
					console.step(DebugStepMode.IN);
				}, new KeyCombination(GLFW.GLFW_KEY_F4)),
				keybindStepNext = new KeybindEntry("Step Next", action -> {
					if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_REPEAT) return;
					if (console == null) return;
					console.step(DebugStepMode.NEXT);
				}, new KeyCombination(GLFW.GLFW_KEY_F5)),
				keybindStepOut = new KeybindEntry("Step Out", action -> {
					if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_REPEAT) return;
					if (console == null) return;
					console.step(DebugStepMode.OUT);
				}, new KeyCombination(GLFW.GLFW_KEY_F6))))));
		inspector = new MachineInspector();
		codeViewer = new CodeViewer();
		controller = new MachineController();

		GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
			if (keybinds.getConfiguringKeybind() != null) {
				keybinds.glfwEvent(key, mods, action);
			} else {
				KeybindEntry entry = keybinds.keybindFromGlfw(key, mods);
				if (entry != null) entry.trigger(action);
			}
		});
	}

	@Override
	public void process() {
		menuBar();
		w4Windows();
		debuggerWindows();
		if (showAboutTinyWasmR) AboutTinyWasmR.window();
		if (showAboutImGui) ImGui.showAboutWindow();

		if (ImGuiFileDialog.display("wasm4cartidge", ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
			if (ImGuiFileDialog.isOk()) {
				File file = ImGuiFileDialog.getSelection().values().stream().findAny().map(File::new).orElse(null);
				if (file != null) loadCartridge(file);
			}

			ImGuiFileDialog.close();
		}

		onFrame();
	}

	private void menuBar() {
		if (ImGui.beginMainMenuBar()) {
			if (ImGui.beginMenu("File")) {
				if (ImGui.menuItem("Open WASM-4 cartridge")) {
					ImGuiFileDialog.openModal("wasm4cartidge", "Open WASM-4 cartridge", ".wasm,.was,.*", ".", 1);
				}

				ImGui.endMenu();
			}

			if (ImGui.beginMenu("WASM-4")) {
				w4WindowMenu();
				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Debugger")) {
				if (ImGui.menuItem(
					console != null && !console.isRunning() ? "Resume" : "Pause", keybindPause.getKeybindNames(), false,
					console != null)) {
					if (console.isRunning()) console.pause();
					else console.resume();
				}

				ImGui.separator();

				if (ImGui.menuItem("Step Into", keybindStepIn.getKeybindNames(), false, console != null))
					console.step(DebugStepMode.IN);
				if (ImGui.menuItem("Step Next", keybindStepNext.getKeybindNames(), false, console != null))
					console.step(DebugStepMode.NEXT);
				if (ImGui.menuItem("Step Out", keybindStepOut.getKeybindNames(), false, console != null))
					console.step(DebugStepMode.OUT);

				ImGui.separator();
				debuggerWindowMenu();
				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Window")) {
				w4WindowMenu();
				ImGui.separator();
				debuggerWindowMenu();
				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Help")) {
				if (ImGui.beginMenu("Documentations")) {
					ImGui.menuItem("WASM-4 docs");
					ImGui.menuItem("TinyWasmR W4");
					ImGui.menuItem("TinyWasmR Engine");
					ImGui.menuItem("TinyWasmR Debugger");
					ImGui.endMenu();
				}

				if (ImGui.beginMenu("About")) {
					if (ImGui.menuItem("TinyWasmR", showAboutTinyWasmR)) showAboutTinyWasmR = !showAboutTinyWasmR;
					if (ImGui.menuItem("Dear ImGui", showAboutImGui)) showAboutImGui = !showAboutImGui;
					ImGui.endMenu();
				}

				ImGui.menuItem("Source code");
				ImGui.endMenu();
			}

			ImGui.endMainMenuBar();
		}
	}

	private void w4WindowMenu() {
		if (ImGui.menuItem("WASM-4 Screen", showScreen)) showScreen = !showScreen;
		if (ImGui.menuItem("Keybinds", showKeybinds)) showKeybinds = !showKeybinds;
	}

	private void debuggerWindowMenu() {
		if (ImGui.menuItem("Machine Inspector", showInspector)) showInspector = !showInspector;
		if (ImGui.menuItem("Code Viewer", showCodeViewer)) showCodeViewer = !showCodeViewer;
		if (ImGui.menuItem("Machine Controller", showController)) showController = !showController;
	}

	private void w4Windows() {
		if (showScreen) {
			if (ImGui.begin("WASM-4 Screen", ImGuiWindowFlags.AlwaysAutoResize))
				screen.screen(console != null ? console.getFramebuffer() : null, W4Framebuffer.SCREEN_SIZE * 3);
			ImGui.end();
		}

		if (showKeybinds) {
			if (ImGui.begin("Keybinds")) keybinds.imgui();
			ImGui.end();
		}
	}

	private void debuggerWindows() {
		if (showInspector) {
			if (ImGui.begin("Machine Inspector")) inspector.inspector(console);
			ImGui.end();
		}

		if (showCodeViewer) {
			if (ImGui.begin("Code Viewer")) codeViewer.viewer(console, inspector.getSelectedFrame());
			ImGui.end();
		}

		if (showController) {
			if (ImGui.begin("Machine Controller", ImGuiWindowFlags.AlwaysAutoResize))
				controller.controller(console);
			ImGui.end();
		}
	}

	public void loadCartridge(File cartfile) {
		try (FileInputStream stream = new FileInputStream(cartfile)) {
			System.out.println("Loading %s cartidge...".formatted(cartfile));
			WasmModule cartModule = BinaryModuleParser.parse(stream);
			console = W4.create(cartModule, null, System.out::println);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void onFrame() {
		if (console != null) console.update();
	}
}
