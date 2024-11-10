package tinywasmr.w4;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import tinywasmr.dbg.gui.MachineController;
import tinywasmr.dbg.gui.MachineInspector;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.parser.binary.BinaryModuleParser;
import tinywasmr.w4.gui.FilePicker;
import tinywasmr.w4.gui.W4Screen;

public class Main extends Application {
	// GUI
	private ImFont font;
	private W4Screen screen;
	private FilePicker filePicker;

	private W4 console;

	// Debugger
	private MachineInspector inspector;
	private MachineController controller;

	public static void main(String[] args) {
		launch(new Main());
	}

	public Main() {
		filePicker = new FilePicker(new File("."), file -> file.isDirectory() ||
			(file.getName().endsWith(".wasm") || file.getName().endsWith(".was")));
	}

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
		inspector = new MachineInspector();
		controller = new MachineController();
	}

	@Override
	public void process() {
		if (font != null) ImGui.pushFont(font);

		if (ImGui.beginMainMenuBar()) {
			if (ImGui.beginMenu("File")) {
				if (ImGui.beginMenu("Open WASM-4 cartidge")) {
					if (filePicker.imgui(500, 200) && filePicker.getFile().isFile())
						loadCartridge(filePicker.getFile());
					ImGui.endMenu();
				}

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

				ImGui.menuItem("Source code");
				ImGui.endMenu();
			}

			ImGui.endMainMenuBar();
		}

		if (ImGui.begin("Machine Inspector")) {
			inspector.inspector(console);
			ImGui.end();
		}

		if (ImGui.begin("Machine Controller", ImGuiWindowFlags.AlwaysAutoResize)) {
			controller.controller(console);
			ImGui.end();
		}

		if (ImGui.begin("WASM-4 Screen", ImGuiWindowFlags.AlwaysAutoResize)) {
			screen.screen(
				console != null ? console.getEnv().object().getFramebuffer() : null,
				W4Framebuffer.SCREEN_SIZE * 3);
			ImGui.end();
		}

		if (font != null) ImGui.popFont();
		onFrame();
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
