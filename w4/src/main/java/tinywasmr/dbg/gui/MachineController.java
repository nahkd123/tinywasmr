package tinywasmr.dbg.gui;

import imgui.ImGui;

public class MachineController {
	public static void controller(boolean state, Runnable resume, Runnable pause, Runnable stepIn, Runnable stepNext, Runnable stepOut) {
		if (ImGui.button(state ? "Pause Execution" : "Resume Execution")) {
			Runnable runnable = state ? pause : resume;
			runnable.run();
		}

		ImGui.separator();

		if (ImGui.button("Step into")) stepIn.run();
		ImGui.sameLine();
		if (ImGui.button("Step next")) stepNext.run();
		ImGui.sameLine();
		if (ImGui.button("Step out")) stepOut.run();
	}
}
