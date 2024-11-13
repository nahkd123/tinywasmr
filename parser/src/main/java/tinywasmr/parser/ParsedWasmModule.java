package tinywasmr.parser;

import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.global.GlobalDecl;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.module.memory.DataSegment;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.table.ElementSegment;
import tinywasmr.engine.module.table.TableDecl;

public class ParsedWasmModule implements WasmModule {
	private List<CustomSection> custom = new ArrayList<>();
	private List<DataSegment> data = new ArrayList<>();
	private List<ElementSegment> elements = new ArrayList<>();
	private List<ImportDecl> imports = new ArrayList<>();
	private List<ExportDecl> exports = new ArrayList<>();
	private List<TableDecl> tables = new ArrayList<>();
	private List<MemoryDecl> memories = new ArrayList<>();
	private List<GlobalDecl> globals = new ArrayList<>();
	private List<FunctionDecl> functions = new ArrayList<>();
	private FunctionDecl start = null;

	@Override
	public List<CustomSection> custom() {
		return custom;
	}

	@Override
	public List<DataSegment> dataSegments() {
		return data;
	}

	@Override
	public List<ElementSegment> elementSegments() {
		return elements;
	}

	@Override
	public List<ImportDecl> declaredImports() {
		return imports;
	}

	@Override
	public List<ExportDecl> declaredExports() {
		return exports;
	}

	@Override
	public List<TableDecl> declaredTables() {
		return tables;
	}

	@Override
	public List<MemoryDecl> declaredMemories() {
		return memories;
	}

	@Override
	public List<GlobalDecl> declaredGlobals() {
		return globals;
	}

	@Override
	public List<FunctionDecl> declaredFunctions() {
		return functions;
	}

	@Override
	public FunctionDecl startFunction() {
		return start;
	}

	public void setStart(FunctionDecl start) { this.start = start; }
}
