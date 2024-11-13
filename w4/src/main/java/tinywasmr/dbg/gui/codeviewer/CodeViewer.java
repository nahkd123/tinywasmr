package tinywasmr.dbg.gui.codeviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import tinywasmr.dbg.AutoDebugSymbols;
import tinywasmr.dbg.DebugInterface;
import tinywasmr.dbg.DebugSymbols;
import tinywasmr.engine.exec.frame.BlockFrame;
import tinywasmr.engine.exec.frame.ExternalFrame;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.frame.IfFrame;
import tinywasmr.engine.exec.frame.LoopFrame;
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
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.ImportFunctionDecl;
import tinywasmr.engine.module.func.ModuleFunctionDecl;
import tinywasmr.engine.module.func.extern.ExternalFunctionDecl;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.type.value.ValueType;

public class CodeViewer {
	private AutoDebugSymbols autoSymbols = new AutoDebugSymbols();
	private WasmModule module = null;
	private FunctionDecl function = null;
	private List<Instruction> scope = new ArrayList<>();

	private Consumer<MemoryDecl> onMemoryClick;

	public CodeViewer(Consumer<MemoryDecl> onMemoryClick) {
		this.onMemoryClick = onMemoryClick;
	}

	public void openModule(WasmModule module) {
		if (this.module == module) return;
		this.module = module;
		this.function = null;
		this.scope = new ArrayList<>();
	}

	public void openFunction(FunctionDecl function) {
		if (this.function == function) return;
		openModule(function instanceof ModuleFunctionDecl m ? m.module() : null);
		this.function = function;
	}

	public void pushScope(Instruction scope) {
		this.scope.add(scope);
	}

	public void popScope() {
		if (this.scope.size() > 0) this.scope.remove(this.scope.size() - 1);
	}

	public void viewer(DebugSymbols symbols, DebugInterface debug) {
		if (symbols == null) symbols = autoSymbols;

		if (module == null && function == null) {
			ImGui.text("Open a module to explore.");
			return;
		}

		breadcrumb(symbols);
		ImGui.separator();

		if (scope.size() > 0) {
			Instruction insn = scope.get(scope.size() - 1);
			instruction(insn, symbols, debug);
		} else if (function != null) {
			if (function instanceof ImportFunctionDecl importFunction) {
				ImGui.text("This function is imported from %s::%s.".formatted(
					importFunction.declaration().module(),
					importFunction.declaration().name()));
			} else if (function instanceof ModuleFunctionDecl moduleFunction) {
				for (int i = 0; i < moduleFunction.body().size(); i++) {
					ImGui.pushID(i);
					instruction(moduleFunction.body().get(i), symbols, debug);
					ImGui.popID();
				}
			} else if (function instanceof ExternalFunctionDecl) {
				ImGui.text("This function is external.");
			} else {
				ImGui.text("This function can't be disassembled.");
			}
		} else if (module != null) {
			//
			module(module, symbols);
		}
	}

	public void breadcrumb(DebugSymbols symbols) {
		String moduleName = module != null ? symbols.nameOf(module) : "static";

		if (ImGui.button(moduleName)) {
			this.function = null;
			this.scope = new ArrayList<>();
		}

		if (function != null) {
			ImGui.sameLine();
			if (ImGui.button("%s()".formatted(symbols.nameOf(function)))) this.scope = new ArrayList<>();

			for (int i = 0; i < scope.size(); i++) {
				Instruction insn = scope.get(i);
				String name = nameOf(insn, symbols);

				ImGui.sameLine();
				if (ImGui.button(name)) {
					for (int j = scope.size() - 1; j >= i; j--) scope.remove(j);
					break; // avoid concurrent modification (hopefully)
				}
			}
		}
	}

	public void module(WasmModule module, DebugSymbols symbols) {
		if (ImGui.treeNodeEx("Module Imports", ImGuiTreeNodeFlags.CollapsingHeader | ImGuiTreeNodeFlags.DefaultOpen,
			"Imports (%d)".formatted(module.declaredImports().size()))) {
			for (ImportDecl decl : module.declaredImports()) ImGui.text("%s::%s".formatted(decl.module(), decl.name()));
		}

		if (ImGui.treeNodeEx("Module Exports", ImGuiTreeNodeFlags.CollapsingHeader | ImGuiTreeNodeFlags.DefaultOpen,
			"Exports (%d)".formatted(module.declaredExports().size()))) {
			for (ExportDecl decl : module.declaredExports()) ImGui.text(decl.name());
		}

		if (ImGui.treeNodeEx("Module Memories", ImGuiTreeNodeFlags.CollapsingHeader | ImGuiTreeNodeFlags.DefaultOpen,
			"Memories (%d)".formatted(module.declaredMemories().size()))) {
			for (int i = 0; i < module.declaredMemories().size(); i++) {
				MemoryDecl memory = module.declaredMemories().get(i);
				if (ImGui.selectable(symbols.nameOf(memory))) onMemoryClick.accept(memory);
			}
		}

		if (ImGui.treeNodeEx("Module Functions", ImGuiTreeNodeFlags.CollapsingHeader | ImGuiTreeNodeFlags.DefaultOpen,
			"Functions (%d)".formatted(module.declaredFunctions().size()))) {
			for (int i = 0; i < module.declaredFunctions().size(); i++) {
				FunctionDecl function = module.declaredFunctions().get(i);
				if (ImGui.selectable(nameOf(function, symbols))) this.function = function;
			}
		}
	}

	public boolean isExecutingInstruction(Instruction insn, DebugInterface debug) {
		if (debug == null) return false;

		for (Frame frame : debug.getMachine().getFrameStack()) {
			if (frame instanceof ExternalFrame || frame.isFrameFinished()) continue;
			boolean isLast = debug.getMachine().peekFrame() == frame;

			if (frame instanceof FunctionFrame function) {
				if (!(function.getDeclaration() instanceof ModuleFunctionDecl decl)) continue;
				if (decl.body().size() == 0) continue;
				Instruction exec = decl.body().get(function.getStep() + (isLast ? 0 : -1));
				if (insn == exec) return true;
			}

			if (frame instanceof BlockFrame block) {
				if (block.getBlock().instructions().size() == 0) continue;
				Instruction exec = block.getBlock().instructions().get(block.getStep() + (isLast ? 0 : -1));
				if (insn == exec) return true;
			}

			if (frame instanceof LoopFrame block) {
				if (block.getBlock().instructions().size() == 0) continue;
				Instruction exec = block.getBlock().instructions().get(block.getStep() + (isLast ? 0 : -1));
				if (insn == exec) return true;
			}

			if (frame instanceof IfFrame block) {
				List<Instruction> branch = block.isTrueBranch()
					? block.getBlock().truePath()
					: block.getBlock().falsePath();
				if (branch.size() == 0) continue;
				Instruction exec = branch.get(block.getStep() + (isLast ? 0 : -1));
				if (insn == exec) return true;
			}
		}

		return false;
	}

	public void instruction(Instruction insn, DebugSymbols symbols, DebugInterface debug) {
		ImGui.beginGroup();
		// char insnPointerChar = isExecutingInstruction(insn, debug) ? '>' : ' ';
		char insnPointerChar = ' ';

		if (insn instanceof LoadInsn load) {
			ImGui.text("%s %s".formatted(insnPointerChar, loadInsnTypeName(load)));
			ImGui.sameLine();
			ImGui.smallButton("$%s".formatted(symbols.nameOf(load.memory())));
		} else if (insn instanceof StoreInsn store) {
			ImGui.text("%s %s".formatted(insnPointerChar, storeInsnTypeName(store)));
			ImGui.sameLine();
			ImGui.smallButton("$%s".formatted(symbols.nameOf(store.memory())));
		} else if (insn instanceof BlockInsn block) {
			ImGui.text("%s block {".formatted(insnPointerChar));

			if (scope.size() == 0 || scope.get(scope.size() - 1) != insn) {
				ImGui.sameLine();
				if (ImGui.smallButton("scope")) pushScope(block);
			}

			ImGui.indent();

			for (int i = 0; i < block.instructions().size(); i++) {
				ImGui.pushID(i);
				instruction(block.instructions().get(i), symbols, debug);
				ImGui.popID();
			}

			ImGui.unindent();
			ImGui.text("  }");
		} else if (insn instanceof LoopInsn block) {
			ImGui.text("%s loop {".formatted(insnPointerChar));

			if (scope.size() == 0 || scope.get(scope.size() - 1) != insn) {
				ImGui.sameLine();
				if (ImGui.smallButton("scope")) pushScope(block);
			}

			ImGui.indent();

			for (int i = 0; i < block.instructions().size(); i++) {
				ImGui.pushID(i);
				instruction(block.instructions().get(i), symbols, debug);
				ImGui.popID();
			}

			ImGui.unindent();
			ImGui.text("  }");
		} else if (insn instanceof IfInsn block) {
			ImGui.text("%s if {".formatted(insnPointerChar));

			if (scope.size() == 0 || scope.get(scope.size() - 1) != insn) {
				ImGui.sameLine();
				if (ImGui.smallButton("scope")) pushScope(block);
			}

			ImGui.indent();

			for (int i = 0; i < block.truePath().size(); i++) {
				ImGui.pushID(i);
				instruction(block.truePath().get(i), symbols, debug);
				ImGui.popID();
			}

			ImGui.unindent();
			if (block.falsePath().size() == 0) {
				ImGui.text("  }");
			} else {
				ImGui.text("  } else {");
				ImGui.indent();

				for (int i = 0; i < block.falsePath().size(); i++) {
					ImGui.pushID(i);
					instruction(block.falsePath().get(i), symbols, debug);
					ImGui.popID();
				}

				ImGui.unindent();
				ImGui.text("  }");
			}
		} else {
			ImGui.text("%s %s".formatted(insnPointerChar, nameOf(insn, symbols)));
		}

		ImGui.endGroup();
	}

	public String nameOf(FunctionDecl function, DebugSymbols symbols) {
		String name = symbols.nameOf(function);
		String params = function.type().inputs().types().stream()
			.map(ValueType::toString)
			.collect(Collectors.joining(", "));
		String results = function.type().outputs().types().size() == 0
			? "void"
			: function.type().outputs().types().stream()
				.map(ValueType::toString)
				.collect(Collectors.joining(", "));
		return "%s(%s): %s".formatted(name, params, results);
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
