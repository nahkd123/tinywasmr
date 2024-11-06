package tinywasmr.engine.exec.instance;

import tinywasmr.engine.module.export.ExportDecl;

public record Export(ExportDecl declaration, String name, Exportable value) {
	public Export(ExportDecl declaration, Exportable value) {
		this(declaration, declaration.name(), value);
	}

	/**
	 * <p>
	 * Get the instance that holds the exported value.
	 * </p>
	 */
	public Instance instance() {
		return value().instance();
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
}
