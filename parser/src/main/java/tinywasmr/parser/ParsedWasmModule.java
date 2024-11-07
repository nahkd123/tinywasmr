package tinywasmr.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.module.table.TableDecl;

public class ParsedWasmModule implements WasmModule {
	private List<CustomSection> custom = new ArrayList<>();
	private List<ImportDecl> imports = new ArrayList<>();
	private List<ExportDecl> exports = new ArrayList<>();
	private List<TableDecl> tables = new ArrayList<>();
	private List<FunctionDecl> functions = new ArrayList<>();

	@Override
	public List<CustomSection> custom() {
		return custom;
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
	public Collection<FunctionDecl> declaredFunctions() {
		return functions;
	}
}
