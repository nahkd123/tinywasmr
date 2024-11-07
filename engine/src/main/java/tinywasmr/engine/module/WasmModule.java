package tinywasmr.engine.module;

import java.util.Collection;

import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.ModuleFunctionDecl;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.module.table.TableDecl;

/**
 * <p>
 * Represent a WebAssembly module. Despite being called "WebAssembly", it is not
 * just limited to web platform.
 * </p>
 * <p>
 * Initially, WebAssembly module can't be used right away. In order to use the
 * module, you need to instantiate it to {@link Instance}. This is because
 * {@link Instance} is used to hold the global data, like global variables,
 * memories and tables. Module, on the other hand, is just a view that follows
 * either text or binary format.
 * </p>
 * <p>
 * A single WebAssembly module contains the following components:
 * <ul>
 * <li>Type Declarations</li>
 * <li>Function Declarations</li>
 * <li>Table Declarations</li>
 * <li>Memory Declarations</li>
 * <li>Global Declarations</li>
 * <li>Element Segments (table initializer)</li>
 * <li>Data Segments (memory initializer)</li>
 * <li>Start Function (initializer)</li>
 * <li>Import Declarations (function, table, memory, global)</li>
 * <li>Export Declarations (function, table, memory, global)</li>
 * </ul>
 * </p>
 */
public interface WasmModule {
	Collection<CustomSection> custom();

	Collection<ImportDecl> declaredImports();

	Collection<ExportDecl> declaredExports();

	Collection<TableDecl> declaredTables();

	/**
	 * <p>
	 * Get a collection of declared functions in this module. Depending on module
	 * type, the declared function can be external or declared in code (which is
	 * {@link ModuleFunctionDecl}).
	 * </p>
	 * <p>
	 * If the module is loaded from text or binary, this list should always be
	 * {@link ModuleFunctionDecl}. If the module is external (provided by embedder),
	 * this list can be anything that implements {@link FunctionDecl}.
	 * </p>
	 */
	Collection<FunctionDecl> declaredFunctions();
}
