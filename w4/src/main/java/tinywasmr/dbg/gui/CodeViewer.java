package tinywasmr.dbg.gui;

import imgui.ImGui;
import tinywasmr.dbg.DebugInterface;
import tinywasmr.dbg.DebugSymbols;
import tinywasmr.engine.exec.value.NumberF32Value;
import tinywasmr.engine.exec.value.NumberF64Value;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.value.NumberI64Value;
import tinywasmr.engine.insn.ConstInsn;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.insn.control.BlockInsn;
import tinywasmr.engine.insn.control.BranchIfInsn;
import tinywasmr.engine.insn.control.BranchInsn;
import tinywasmr.engine.insn.control.CallInsn;
import tinywasmr.engine.insn.control.ControlInsn;
import tinywasmr.engine.insn.control.IfInsn;
import tinywasmr.engine.insn.control.LoopInsn;
import tinywasmr.engine.insn.memory.LoadInsn;
import tinywasmr.engine.insn.memory.StoreInsn;
import tinywasmr.engine.insn.variable.GlobalInsn;
import tinywasmr.engine.insn.variable.LocalInsn;

public class CodeViewer {
	public CodeViewer() {}

	public void viewer(DebugInterface debug) {
		if (debug == null) {
			ImGui.text("Waiting for debugger...");
			return;
		}

		// TODO
	}

	public void instruction(Instruction insn, boolean current, DebugSymbols symbols) {
		ImGui.beginGroup();

		if (insn instanceof LoadInsn load) {
			ImGui.text("%s %s".formatted(current ? '>' : ' ', loadInsnTypeName(load)));
			ImGui.sameLine();
			ImGui.smallButton("$%s".formatted(symbols.nameOf(load.memory())));
		} else if (insn instanceof StoreInsn store) {
			ImGui.text("%s %s".formatted(current ? '>' : ' ', storeInsnTypeName(store)));
			ImGui.sameLine();
			ImGui.smallButton("$%s".formatted(symbols.nameOf(store.memory())));
		} else {
			ImGui.text("%s %s".formatted(current ? '>' : ' ', nameOf(insn, symbols)));
		}

		ImGui.sameLine();
		ImGui.text("");
		ImGui.endGroup();
	}

	public String nameOf(Instruction insn, DebugSymbols symbols) {
		if (insn instanceof ConstInsn constant) {
			if (constant.value() instanceof NumberI32Value i32) return "i32.const %d".formatted(i32.i32());
			if (constant.value() instanceof NumberI64Value i64) return "i64.const %d".formatted(i64.i64());
			if (constant.value() instanceof NumberF32Value f32) return "f32.const %f".formatted(f32.f32());
			if (constant.value() instanceof NumberF64Value f64) return "f64.const %f".formatted(f64.f64());
			return "<%s>.const %s".formatted(constant.value().type(), constant.value());
		}

		if (insn instanceof ControlInsn control) return switch (control) {
		case NOP -> "nop";
		case RETURN -> "return";
		case UNREACHABLE -> "unreachable";
		default -> control.toString();
		};

		if (insn instanceof LocalInsn local) return "%s %d".formatted(
			switch (local.type()) {
			case GET -> "local.get";
			case SET -> "local.set";
			case TEE -> "local.tee";
			default -> local.type().toString();
			},
			local.index());

		if (insn instanceof GlobalInsn global) return "%s $%s".formatted(
			switch (global.type()) {
			case GET -> "global.get";
			case SET -> "global.set";
			default -> global.type().toString();
			},
			symbols.nameOf(global.global()));

		if (insn instanceof LoadInsn load) return "%s $%s offset=0x%08x".formatted(
			loadInsnTypeName(load),
			symbols.nameOf(load.memory()),
			load.memarg().offset());

		if (insn instanceof StoreInsn store) return "%s $%s offset=0x%08x".formatted(
			storeInsnTypeName(store),
			symbols.nameOf(store.memory()),
			store.memarg().offset());

		if (insn instanceof BlockInsn) return "block { ... }";
		if (insn instanceof IfInsn) return "if { ... }";
		if (insn instanceof LoopInsn) return "loop { ... }";
		if (insn instanceof BranchInsn br) return "br %d".formatted(br.nestIndex());
		if (insn instanceof BranchIfInsn br) return "br_if %d".formatted(br.nestIndex());
		if (insn instanceof CallInsn call) return "call $%s".formatted(symbols.nameOf(call.function()));

		return insn.toString();
	}

	private String storeInsnTypeName(StoreInsn store) {
		return switch (store.type()) {
		case I32 -> "i32.store";
		case I64 -> "i64.store";
		case F32 -> "f32.store";
		case F64 -> "f64.store";
		case I32_I8 -> "i32.store_8";
		case I32_I16 -> "i32.store_16";
		case I64_I8 -> "i64.store_8";
		case I64_I16 -> "i64.store_16";
		case I64_I32 -> "i64.store_32";
		default -> "store<" + store.type() + ">";
		};
	}

	private String loadInsnTypeName(LoadInsn load) {
		return switch (load.type()) {
		case I32 -> "i32.load";
		case I64 -> "i64.load";
		case F32 -> "f32.load";
		case F64 -> "f64.load";
		case I32_U8 -> "i32.load_8u";
		case I32_S8 -> "i32.load_8s";
		case I32_U16 -> "i32.load_16u";
		case I32_S16 -> "i32.load_16s";
		case I64_U8 -> "i64.load_8u";
		case I64_S8 -> "i64.load_8s";
		case I64_U16 -> "i64.load_16u";
		case I64_S16 -> "i64.load_16s";
		case I64_U32 -> "i64.load_32u";
		case I64_S32 -> "i64.load_32s";
		default -> "load<" + load.type() + ">";
		};
	}
}
