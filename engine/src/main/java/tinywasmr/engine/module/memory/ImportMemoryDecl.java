package tinywasmr.engine.module.memory;

import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.type.MemoryType;

public class ImportMemoryDecl implements MemoryDecl {
	private WasmModule module;
	private MemoryType type;
	private ImportDecl declaration;

	public ImportMemoryDecl(WasmModule module, MemoryType type, ImportDecl declaration) {
		this.module = module;
		this.type = type;
		this.declaration = declaration;
	}

	public WasmModule module() {
		return module;
	}

	public ImportDecl declaration() {
		return declaration;
	}

	@Override
	public MemoryType type() {
		return type;
	}
}
