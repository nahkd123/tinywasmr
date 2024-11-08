package tinywasmr.engine.module.table;

import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.type.TableType;

public class ImportTableDecl implements TableDecl {
	private WasmModule module;
	private TableType type;
	private ImportDecl declaration;

	public ImportTableDecl(WasmModule module, TableType type, ImportDecl declaration) {
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
	public TableType type() {
		return type;
	}
}
