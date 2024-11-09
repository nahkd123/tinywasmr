package tinywasmr.engine.module.global;

import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.type.GlobalType;

public class ImportGlobalDecl implements GlobalDecl {
	private WasmModule module;
	private GlobalType type;
	private ImportDecl declaration;

	public ImportGlobalDecl(WasmModule module, GlobalType type, ImportDecl declaration) {
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
	public GlobalType type() {
		return type;
	}
}
