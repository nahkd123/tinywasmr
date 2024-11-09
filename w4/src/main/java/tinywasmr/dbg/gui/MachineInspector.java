package tinywasmr.dbg.gui;

import imgui.ImGui;
import tinywasmr.engine.exec.frame.BlockFrame;
import tinywasmr.engine.exec.frame.ExternalFrame;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.value.NumberI64Value;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;

public class MachineInspector {
	public static void machine(Machine vm) {
		if (vm == null) {
			ImGui.text("Machine is not loaded");
			return;
		}

		if (ImGui.collapsingHeader("Overview")) {
			ImGui.labelText("Trapped", vm.getTrap() == null ? "No" : vm.getTrap().toString());
			ImGui.labelText("Validation", Boolean.toString(vm.hasRuntimeValidation()));
			ImGui.labelText("Frames", "%d frames".formatted(vm.getFrameStack().size()));
		}

		if (ImGui.collapsingHeader("Frame Stack")) {
			ImGui.indent();
			for (int i = 0; i < vm.getFrameStack().size(); i++) frame(vm.getFrameStack().get(i), i);
			ImGui.unindent();
		}
	}

	public static void frame(Frame frame, int id) {
		String name;

		if (frame instanceof ExternalFrame) name = "extern".formatted(frame.getOperandStack());
		else if (frame instanceof FunctionFrame func) name = "func()".formatted(func);
		else if (frame instanceof BlockFrame) name = "block {...}";
		else name = "unknown";

		if (ImGui.collapsingHeader("%03d: %s".formatted(id, name))) {
			ImGui.indent();

			if (!(frame instanceof ExternalFrame)) {
				ImGui.button("Code browser");

				int currStep = frame.getInsnIndex();
				int maxStep = frame.getExecutingInsns().size();
				ImGui.sameLine();
				ImGui.progressBar(currStep / (float) maxStep, "Insn -> %d out of %d".formatted(currStep, maxStep));
			}

			if (frame instanceof FunctionFrame func) {
				ImGui.labelText("Locals", Integer.toString(func.getLocals().length));
				for (int i = 0; i < func.getLocals().length; i++) {
					String[] display = valueToDisplay(func.getLocals()[i]);
					ImGui.labelText("Local %d".formatted(i), "%9s | %s".formatted(display[0], display[1]));
				}
			}

			ImGui.labelText("Operands", Integer.toString(frame.getOperandStack().size()));
			for (int i = 0; i < frame.getOperandStack().size(); i++) {
				String[] display = valueToDisplay(frame.getOperandStack().get(i));
				ImGui.labelText("Operand %d".formatted(i), "%9s | %s".formatted(display[0], display[1]));
			}

			ImGui.unindent();
		}
	}

	public static String[] valueToDisplay(Value val) {
		String type, value;

		if (val instanceof NumberI32Value i32) {
			type = "i32.const";
			value = "%08x | %d".formatted(i32.i32(), i32.i32());
		} else if (val instanceof NumberI64Value i64) {
			type = "i64.const";
			value = "%016x | %d".formatted(i64.i64(), i64.i64());
		} else {
			type = "unknown";
			value = val.toString();
		}

		return new String[] { type, value };
	}
}
