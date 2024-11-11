package tinywasmr.dbg.gui;

import imgui.ImGui;

public class AboutTinyWasmR {
	public static void window() {
		if (ImGui.begin("About TinyWasmR")) content();
		ImGui.end();
	}

	public static void content() {
		ImGui.text("TinyWasmR by nahkd123");
		ImGui.text("TinyWasmR is an implementation of WebAssembly in Java");
		ImGui.text("TinyWasmR is licensed under MIT license");
	}
}
