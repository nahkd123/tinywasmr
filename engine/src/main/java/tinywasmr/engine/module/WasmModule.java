package tinywasmr.engine.module;

import java.util.List;

import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.InitializerFunctionDecl;
import tinywasmr.engine.module.global.GlobalDecl;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.module.memory.DataSegment;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.table.TableDecl;

public interface WasmModule {
	List<CustomSection> custom();

	List<DataSegment> dataSegments();

	List<ImportDecl> declaredImports();

	List<ExportDecl> declaredExports();

	List<TableDecl> declaredTables();

	List<MemoryDecl> declaredMemories();

	List<GlobalDecl> declaredGlobals();

	List<FunctionDecl> declaredFunctions();

	default InitializerFunctionDecl initFunction() {
		return new InitializerFunctionDecl(this);
	}
}
