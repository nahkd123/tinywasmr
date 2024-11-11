package tinywasmr.w4;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

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

	// Debugger GUI
	private MachineInspector inspector;
	private MachineController controller;
	private boolean showInspector = false;
	private boolean showController = false;

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
		io.setIniFilename(null);
		io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
		io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
		io.getFonts().addFontFromFileTTF("C:\\Windows\\Fonts\\consola.ttf", 16f);

		screen = new W4Screen();
		keybinds = new W4Keybinds(List.of(
			new KeybindGroup("Gamepad #1", List.of(
				new KeybindEntry("Up", action -> {
					boolean down = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;
					if (console == null) return;
					console.getInput().setGamepad(0, W4Input.BUTTON_UP, down);
				}, new KeyCombination(GLFW.GLFW_KEY_W), new KeyCombination(GLFW.GLFW_KEY_UP)),
				new KeybindEntry("Down", action -> {
					boolean down = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;
					if (console == null) return;
					console.getInput().setGamepad(0, W4Input.BUTTON_DOWN, down);
				}, new KeyCombination(GLFW.GLFW_KEY_S), new KeyCombination(GLFW.GLFW_KEY_DOWN)),
				new KeybindEntry("Left", action -> {
					boolean down = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;
					if (console == null) return;
					console.getInput().setGamepad(0, W4Input.BUTTON_LEFT, down);
				}, new KeyCombination(GLFW.GLFW_KEY_A), new KeyCombination(GLFW.GLFW_KEY_LEFT)),
				new KeybindEntry("Right", action -> {
					boolean down = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;
					if (console == null) return;
					console.getInput().setGamepad(0, W4Input.BUTTON_RIGHT, down);
				}, new KeyCombination(GLFW.GLFW_KEY_D), new KeyCombination(GLFW.GLFW_KEY_RIGHT)),
				new KeybindEntry("Button 1", action -> {
					boolean down = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;
					if (console == null) return;
					console.getInput().setGamepad(0, W4Input.BUTTON_1, down);
				}, new KeyCombination(GLFW.GLFW_KEY_X)),
				new KeybindEntry("Button 2", action -> {
					boolean down = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;
					if (console == null) return;
					console.getInput().setGamepad(0, W4Input.BUTTON_2, down);
				}, new KeyCombination(GLFW.GLFW_KEY_Z))))));
		inspector = new MachineInspector();
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
					console != null && !console.isRunning() ? "Resume" : "Pause", "Break", false,
					console != null)) {
					if (console.isRunning()) console.pause();
					else console.resume();
				}

				ImGui.separator();

				if (ImGui.menuItem("Step Into", "F4", false, console != null)) console.step(DebugStepMode.IN);
				if (ImGui.menuItem("Step Next", "F5", false, console != null)) console.step(DebugStepMode.NEXT);
				if (ImGui.menuItem("Step Out", "F6", false, console != null)) console.step(DebugStepMode.OUT);

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
