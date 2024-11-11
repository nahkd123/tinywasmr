package tinywasmr.dbg.gui;

import java.util.Collections;
import java.util.List;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import tinywasmr.dbg.AutoDebugSymbols;
import tinywasmr.dbg.DebugInterface;
import tinywasmr.dbg.DebugSymbols;
import tinywasmr.dbg.ValueDisplayMode;
import tinywasmr.engine.exec.frame.BlockFrame;
import tinywasmr.engine.exec.frame.ExternalFrame;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.ConstInsn;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.insn.control.BranchBaseInsn;
import tinywasmr.engine.insn.variable.LocalInsn;
import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.RefType;
import tinywasmr.engine.type.value.ValueType;
import tinywasmr.engine.type.value.VectorType;

public class MachineInspector {
	private int[] valueDisplayMode = new int[] { 1 };
	private ValueDisplayMode[] allValueDisplayModes = ValueDisplayMode.values();

	private Frame selectedFrame = null;
	private ImString functionName = new ImString(1024);

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

	public void codeViewer(DebugInterface debug) {
		if (debug == null) {
			selectedFrame = null;
			ImGui.text("Waiting for debugger...");
			return;
		}

		if (selectedFrame == null) {
			ImGui.text("Open in Machine Inspector to browse code!");
			ImGui.text("Click on 'Go to' button to open.");
			return;
		}

		if (ImGui.button("Close frame")) {
			selectedFrame = null;
			return;
		}

		ImGui.sameLine();

		if (selectedFrame instanceof FunctionFrame ff && debug.getSymbols() instanceof AutoDebugSymbols auto) {
			if (ImGui.inputText("Function name", functionName))
				auto.setName(ff.getFunction().declaration(), functionName.get());
		} else {
			ImGui.text("%s".formatted(selectedFrame));
		}

		List<Instruction> insns = selectedFrame.getExecutingInsns();

		for (int i = 0; i < insns.size(); i++) {
			Instruction insn = insns.get(i);
			boolean isCurrent = selectedFrame.getInsnIndex() == i;

			ImGui.beginGroup();
			ImGui.text("%s %s".formatted(isCurrent ? '>' : ' ', insnToString(insn)));
			ImGui.endGroup();
		}
	}

	public static String insnToString(Instruction insn) {
		if (insn instanceof LocalInsn local) return "local.%s %s".formatted(local.type(), local.index());
		if (insn instanceof ConstInsn constant) return "constant of %s".formatted(constant.value());
		if (insn instanceof BranchBaseInsn br) return "%s %d".formatted(br.getClass().getSimpleName(), br.nestIndex());
		return insn.toString();
	}

	public void overview(Machine vm, DebugSymbols symbols) {
		if (vm == null) {
			ImGui.text("Waiting for debugger...");
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
			ImGui.pushID(id);

			if (!(frame instanceof ExternalFrame)) {
				if (ImGui.button("Go to")) {
					selectedFrame = frame;

					if (frame instanceof FunctionFrame ff) {
						String fname = symbols.nameOf(ff.getFunction().declaration());
						functionName.set(fname);
					}
				}

				int currStep = frame.getInsnIndex();
				int maxStep = frame.getExecutingInsns().size();
				ImGui.sameLine();
				ImGui.progressBar(currStep / (float) maxStep, "Insn -> %d out of %d".formatted(currStep, maxStep));
			}

			if (frame instanceof FunctionFrame func) {
				ImGui.text("Locals: %d".formatted(func.getLocals().length));
				ImGui.indent();

				for (int i = 0; i < func.getLocals().length; i++) {
					String type = typeToString(func.getLocals()[i].type());
					String val = valueDisplay.asString(func.getLocals()[i], symbols);
					ImGui.text("%03d: %4s | %s".formatted(i, type, val));
				}

				ImGui.unindent();
			}

			ImGui.text("Operands: %d".formatted(frame.getOperandStack().size()));

			for (int i = 0; i < frame.getOperandStack().size(); i++) {
				String type = typeToString(frame.getOperandStack().get(i).type());
				String val = valueDisplay.asString(frame.getOperandStack().get(i), symbols);
				ImGui.text("%03d: %4s | %s".formatted(i, type, val));
			}

			ImGui.popID();
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
