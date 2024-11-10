package tinywasmr.dbg.gui;

import imgui.ImGui;
import tinywasmr.dbg.DebugInterface;
import tinywasmr.dbg.DebugStepMode;
import tinywasmr.engine.exec.trap.ModuleTrap;
import tinywasmr.engine.exec.vm.Machine;

public class MachineController {
	private int[] insnPerSec = new int[] { -1 };
	private DebugInterface lastDebug = null;

	public void controller(DebugInterface debug) {
		if (debug == null) {
			ImGui.text("Waiting for debugger...");
			lastDebug = debug;
			return;
		}

		if (debug != lastDebug) {
			insnPerSec[0] = debug.getExecutionSpeed();
			// TODO
		}

		if (ImGui.button(debug.isRunning() ? "Pause" : "Resume")) {
			if (debug.isRunning()) debug.pause();
			else debug.resume();
		}

		ImGui.sameLine();
		if (ImGui.button(debug.getMachine().getTrap() == null ? "Trap" : "Untrap")) {
			Machine vm = debug.getMachine();
			vm.setTrap(vm.getTrap() != null ? null : new ModuleTrap());
		}

		if (ImGui.sliderInt("Speed", insnPerSec, -1, 1000, insnPerSec[0] == -1
			? "Unlimited"
			: "%d insn/sec")) debug.setExecutionSpeed(insnPerSec[0]);

		ImGui.separator();

		if (ImGui.button("Step into")) debug.step(DebugStepMode.IN);
		ImGui.sameLine();
		if (ImGui.button("Step next")) debug.step(DebugStepMode.NEXT);
		ImGui.sameLine();
		if (ImGui.button("Step out")) debug.step(DebugStepMode.OUT);
	}
}
