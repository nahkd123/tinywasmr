package tinywasmr.dbg.gui;

import java.util.Collections;
import java.util.List;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import tinywasmr.dbg.DebugInterface;
import tinywasmr.dbg.DebugSymbols;
import tinywasmr.dbg.ValueDisplayMode;
import tinywasmr.engine.exec.frame.BlockFrame;
import tinywasmr.engine.exec.frame.ExternalFrame;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.RefType;
import tinywasmr.engine.type.value.ValueType;
import tinywasmr.engine.type.value.VectorType;

public class MachineInspector {
	private int[] valueDisplayMode = new int[] { 1 };
	private ValueDisplayMode[] allValueDisplayModes = ValueDisplayMode.values();

	public void inspector(DebugInterface debug) {
		ValueDisplayMode valueDisplay = allValueDisplayModes[valueDisplayMode[0]];
		ImGui.text("Inspector display modes");
		ImGui.sliderInt("Value", valueDisplayMode, 0, allValueDisplayModes.length - 1, valueDisplay.toString());

		ImGui.separator();

		if (ImGui.collapsingHeader("Overview", ImGuiTreeNodeFlags.DefaultOpen)) {
			ImGui.indent();
			overview(
				debug != null ? debug.getMachine() : null,
				debug != null ? debug.getSymbols() : null);
			ImGui.unindent();
		}

		if (ImGui.collapsingHeader("Frame Stack")) {
			ImGui.indent();
			List<Frame> frames = debug != null ? debug.getMachine().getFrameStack() : Collections.emptyList();
			for (int i = 0; i < frames.size(); i++) frame(frames.get(i), i, valueDisplay, debug.getSymbols());
			ImGui.unindent();
		}
	}

	public void overview(Machine vm, DebugSymbols symbols) {
		if (vm == null) {
			ImGui.text("Machine is not loaded");
			return;
		}

		ImGui.labelText("Trapped", vm.getTrap() == null ? "No" : vm.getTrap().toString());
		ImGui.labelText("Validation", Boolean.toString(vm.hasRuntimeValidation()));
		ImGui.labelText("Frames", "%d frames".formatted(vm.getFrameStack().size()));
	}

	public void frame(Frame frame, int id, ValueDisplayMode valueDisplay, DebugSymbols symbols) {
		String name;

		if (frame instanceof ExternalFrame) name = "extern".formatted(frame.getOperandStack());
		else if (frame instanceof FunctionFrame func) name = "  func %s()"
			.formatted(symbols.nameOf(func.getFunction().declaration()));
		else if (frame instanceof BlockFrame) name = " block";
		else name = "unknown";

		if (ImGui.treeNodeEx(id, ImGuiTreeNodeFlags.NoTreePushOnOpen, "%03d: %s".formatted(id, name))) {
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
					String type = typeToString(func.getLocals()[i].type());
					String val = valueDisplay.asString(func.getLocals()[i], symbols);
					ImGui.labelText("Local %d".formatted(i), "%8s | %s".formatted(type, val));
				}
			}

			ImGui.labelText("Operands", Integer.toString(frame.getOperandStack().size()));

			for (int i = 0; i < frame.getOperandStack().size(); i++) {
				String type = typeToString(frame.getOperandStack().get(i).type());
				String val = valueDisplay.asString(frame.getOperandStack().get(i), symbols);
				ImGui.labelText("Operand %d".formatted(i), "%8s | %s".formatted(type, val));
			}

			ImGui.unindent();
		}
	}

	public static String typeToString(ValueType type) {
		if (type instanceof NumberType nt) return switch (nt) {
		case I32 -> "i32";
		case I64 -> "i64";
		case F32 -> "f32";
		case F64 -> "f64";
		};
		if (type instanceof VectorType vt) return switch (vt) {
			case V128 -> "v128";
		};
		if (type instanceof RefType rt) return switch (rt) {
		case FUNC -> "funcref";
		case EXTERN -> "extenref";
		};
		return type.toString();
	}
}
