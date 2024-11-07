package tinywasmr.engine.module.func;

import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.type.FunctionType;

public class ImportFunctionDecl implements FunctionDecl {
	private WasmModule module;
	private FunctionType type;
	private ImportDecl declaration;

	public ImportFunctionDecl(WasmModule module, FunctionType type, ImportDecl declaration) {
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
	public FunctionType type() {
		return type;
	}
}
