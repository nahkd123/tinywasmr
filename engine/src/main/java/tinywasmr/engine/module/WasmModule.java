package tinywasmr.engine.module;

import java.util.List;

import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.table.TableDecl;

public interface WasmModule {
	List<CustomSection> custom();

	List<ImportDecl> declaredImports();

	List<ExportDecl> declaredExports();

	List<TableDecl> declaredTables();

	List<MemoryDecl> declaredMemories();

	List<FunctionDecl> declaredFunctions();
}
