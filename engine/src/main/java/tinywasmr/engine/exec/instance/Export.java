package tinywasmr.engine.exec.instance;

import tinywasmr.engine.exec.global.Global;
import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.module.export.ExportDecl;

public record Export(ExportDecl declaration, String name, Exportable value) {
	public Export(ExportDecl declaration, Exportable value) {
		this(declaration, declaration.name(), value);
	}

	/**
	 * <p>
	 * Quick method to get this export as a function.
	 * </p>
	 */
	public Function asFunction() {
		if (value() instanceof Function function) return function;
		throw new IllegalArgumentException("This export is not a function");
	}

	public Table asTable() {
		if (value() instanceof Table table) return table;
		throw new IllegalArgumentException("This export is not a table");
	}

	public Memory asMemory() {
		if (value() instanceof Memory memory) return memory;
		throw new IllegalArgumentException("This export is not a memory");
	}

	public Global asGlobal() {
		if (value() instanceof Global global) return global;
		throw new IllegalArgumentException("This export is not a global");
	}
}
