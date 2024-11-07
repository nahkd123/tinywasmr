package tinywasmr.engine.module.table;

import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.type.TableType;

public class ModuleTableDecl implements TableDecl {
	private WasmModule module;
	private TableType type;

	public ModuleTableDecl(WasmModule module, TableType type) {
		this.module = module;
		this.type = type;
	}

	public WasmModule module() {
		return module;
	}

	@Override
	public TableType type() {
		return type;
	}
}
