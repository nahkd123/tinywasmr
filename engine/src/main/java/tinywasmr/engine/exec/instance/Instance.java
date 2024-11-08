package tinywasmr.engine.exec.instance;

import java.util.Collection;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.table.TableDecl;

/**
 * <p>
 * Represent an instance. Instances are instantiated from {@link WasmModule} and
 * each instance holds its own globals, memories and tables.
 * </p>
 */
public interface Instance {
	/**
	 * <p>
	 * Get the module that is linked to this instance. If the instance is external,
	 * this will return {@code null}.
	 * </p>
	 */
	WasmModule module();

	/**
	 * <p>
	 * Get a collection of custom sections in the module that linked to this
	 * instance.
	 * </p>
	 */
	default Collection<CustomSection> custom() {
		return module().custom();
	}

	Collection<Function> functions();

	default Function function(FunctionDecl decl) {
		for (Function function : functions()) if (function.declaration() == decl) return function;
		return null;
	}

	Collection<Table> tables();

	default Table table(TableDecl decl) {
		for (Table table : tables()) if (table.declaration() == decl) return table;
		return null;
	}

	Collection<Memory> memories();

	default Memory memory(MemoryDecl decl) {
		for (Memory memory : memories()) if (memory.declaration() == decl) return memory;
		return null;
	}

	Collection<Export> exports();

	/**
	 * <p>
	 * Get the exported symbol from this instance.
	 * </p>
	 */
	default Export export(String name) {
		for (Export exp : exports()) if (exp.name().equals(name)) return exp;
		return null;
	}
}
