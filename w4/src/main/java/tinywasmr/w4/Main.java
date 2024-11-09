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
import tinywasmr.engine.exec.executor.DefaultExecutor;
import tinywasmr.engine.exec.executor.Executor;
import tinywasmr.engine.exec.instance.DefaultInstance;
import tinywasmr.engine.exec.instance.SimpleImporter;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.DefaultMachine;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.extern.ReflectedInstance;
import tinywasmr.extern.ReflectedModule;
import tinywasmr.parser.binary.BinaryModuleParser;
import tinywasmr.w4.gui.FilePicker;

public class Main extends Application {
	private ImFont font;

	// Current game
	private ReflectedModule<W4Environment> envModule;
	private ReflectedInstance<W4Environment> env;
	private DefaultInstance cartridge;
	private Machine machine;
	private Executor executor;

	// Misc
	private FilePicker filePicker;

	public static void main(String[] args) {
		launch(new Main());
	}

	public Main() {
		envModule = new ReflectedModule<>(W4Environment.class);
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
		io.getFonts().addFontFromFileTTF("C:\\Windows\\Fonts\\consola.ttf", 16f);
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

			if (ImGui.beginMenu("WASM-4")) {
				ImGui.menuItem("Quick save", "Ctrl S");
				ImGui.menuItem("Quick load", "Ctrl O");
				ImGui.separator();
				ImGui.menuItem("Configure...");
				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Debug")) {
				ImGui.menuItem("Pause/resume execution", "Break");
				ImGui.menuItem("Step into block", "F4", false, false);
				ImGui.menuItem("Step next instruction", "F5", false, false);
				ImGui.menuItem("Step out of block", "F6", false, false);
				ImGui.separator();
				ImGui.menuItem("Machine inspector");
				ImGui.menuItem("Instance inspector");
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

		// TODO

		if (font != null) ImGui.popFont();
	}

	public void loadCartridge(File cartfile) {
		System.out.println("Loading %s cartidge...".formatted(cartfile));

		try (FileInputStream stream = new FileInputStream(cartfile)) {
			env = envModule.instanceOf(new W4Environment(null, System.out::println));
			WasmModule cartModule = BinaryModuleParser.parse(stream);
			cartridge = new DefaultInstance(cartModule, SimpleImporter.builder().module("env", env).build());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		machine = new DefaultMachine();
		executor = new DefaultExecutor();
		machine.call(cartridge.initFunction(), new Value[0]);
	}
}
