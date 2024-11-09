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
import tinywasmr.dbg.gui.MachineController;
import tinywasmr.dbg.gui.MachineInspector;
import tinywasmr.engine.exec.StepResult;
import tinywasmr.engine.exec.executor.DefaultExecutor;
import tinywasmr.engine.exec.executor.Executor;
import tinywasmr.engine.exec.instance.DefaultInstance;
import tinywasmr.engine.exec.instance.SimpleImporter;
import tinywasmr.engine.exec.trap.ExternalTrap;
import tinywasmr.engine.exec.trap.ModuleTrap;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.DefaultMachine;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.extern.ReflectedInstance;
import tinywasmr.extern.ReflectedModule;
import tinywasmr.parser.binary.BinaryModuleParser;
import tinywasmr.w4.gui.FilePicker;
import tinywasmr.w4.gui.W4Screen;

public class Main extends Application {
	private ImFont font;
	private W4Screen screen;

	// Current game
	private ReflectedModule<W4Environment> envModule;
	private ReflectedInstance<W4Environment> env;
	private DefaultInstance cartridge;
	private Machine machine;
	private Executor executor;

	// Stages and execution
	private boolean running;
	private boolean startStage; // Whether start stage is finished
	// After start stage, we enter update() immediately

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
		io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
		io.getFonts().addFontFromFileTTF("C:\\Windows\\Fonts\\consola.ttf", 16f);

		screen = new W4Screen();
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
			MachineInspector.machine(machine);
			ImGui.end();
		}

		if (ImGui.begin("Machine Controller")) {
			MachineController.controller(
				running,
				() -> running = true,
				() -> running = false,
				this::stepIn, this::stepNext, this::stepOut);
			ImGui.end();
		}

		if (ImGui.begin("WASM-4 Screen")) {
			screen.screen(env != null ? env.object() : null, 320);
			ImGui.end();
		}

		ImGui.showDemoWindow();

		if (font != null) ImGui.popFont();
		onFrame();
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
		running = true;
		startStage = false;
		machine.call(cartridge.initFunction(), new Value[0]);
	}

	public void onFrame() {
		if (!running) return;
		if (executor == null || machine == null) return;
		if (machine.getTrap() != null) return;
		if (machine.peekFrame() == machine.getExternalFrame()) nextStage();

		while (true) {
			StepResult result = executor.step(machine);
			if (result == StepResult.TRAP) return;

			if (machine.peekFrame() == machine.getExternalFrame()) {
				nextStage();
				return;
			}
		}
	}

	public void nextStage() {
		if (!startStage) {
			startStage = true;
			machine.call(cartridge.export("start").asFunction(), new Value[0]);
		} else {
			machine.call(cartridge.export("update").asFunction(), new Value[0]);
		}
	}

	public void stepIn() {
		if (executor == null || machine == null) return;
		if (machine.getTrap() != null) return;
		if (machine.peekFrame() == machine.getExternalFrame()) nextStage();
		else if (executor.step(machine) == StepResult.TRAP) {
			if (machine.getTrap() instanceof ModuleTrap) System.err.println("module trap");
			else if (machine.getTrap() instanceof ExternalTrap extern) {
				extern.throwable().printStackTrace();
				System.err.println("extern trap");
			}
		}
	}

	public void stepNext() {
		if (executor == null || machine == null) return;
		if (machine.getTrap() != null) return;
		if (machine.peekFrame() == machine.getExternalFrame()) nextStage();
		else {
			int height = machine.getFrameStack().size();
			do executor.step(machine); while (machine.getFrameStack().size() > height && machine.getTrap() == null);
		}
	}

	public void stepOut() {
		if (executor == null || machine == null) return;
		if (machine.getTrap() != null) return;
		if (machine.peekFrame() == machine.getExternalFrame()) nextStage();
		else {
			int height = machine.getFrameStack().size();
			do executor.step(machine); while (machine.getFrameStack().size() >= height && machine.getTrap() == null);
		}
	}
}
