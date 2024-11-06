package tinywasmr.engine.exec.instance;

import java.util.Collection;
import java.util.List;

import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.func.FunctionDecl;

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

	/**
	 * <p>
	 * Get an ordered list of all functions that are imported, hidden and exported
	 * from this instance. Ordered list is used here because functions in
	 * WebAssembly binary are indexed, starting with imported functions then
	 * declared functions.
	 * </p>
	 */
	List<Function> functions();

	default Function function(FunctionDecl decl) {
		for (Function function : functions()) if (function.declaration() == decl) return function;
		return null;
	}

	/**
	 * <p>
	 * Get a collection of exported symbols in this instance.
	 * </p>
	 */
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
