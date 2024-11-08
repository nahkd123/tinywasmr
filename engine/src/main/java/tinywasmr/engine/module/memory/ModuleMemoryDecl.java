package tinywasmr.engine.module.memory;

import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.type.MemoryType;

public class ModuleMemoryDecl implements MemoryDecl {
	private WasmModule module;
	private MemoryType type;

	public ModuleMemoryDecl(WasmModule module, MemoryType type) {
		this.module = module;
		this.type = type;
	}

	public WasmModule module() {
		return module;
	}

	@Override
	public MemoryType type() {
		return type;
	}
}
