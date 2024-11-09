package tinywasmr.engine.module.global;

import java.util.List;

import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.type.GlobalType;

public class ModuleGlobalDecl implements GlobalDecl {
	private WasmModule module;
	private GlobalType type;
	private List<Instruction> init;

	public ModuleGlobalDecl(WasmModule module, GlobalType type, List<Instruction> init) {
		this.module = module;
		this.type = type;
		this.init = init;
	}

	public WasmModule module() {
		return module;
	}

	@Override
	public GlobalType type() {
		return type;
	}

	public List<Instruction> init() {
		return init;
	}
}
