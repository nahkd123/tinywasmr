package tinywasmr.engine.exec.instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinywasmr.engine.exec.memory.DefaultMemory;
import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.table.DefaultTable;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.export.FunctionExportDescription;
import tinywasmr.engine.module.export.MemoryExportDescription;
import tinywasmr.engine.module.export.TableExportDescription;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.ImportFunctionDecl;
import tinywasmr.engine.module.memory.ImportMemoryDecl;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.memory.ModuleMemoryDecl;
import tinywasmr.engine.module.table.ImportTableDecl;
import tinywasmr.engine.module.table.ModuleTableDecl;
import tinywasmr.engine.module.table.TableDecl;
import tinywasmr.engine.type.FunctionType;

public class DefaultInstance implements Instance {
	private WasmModule module;
	private List<Function> allFunctions;
	private List<Table> allTables;
	private List<Memory> allMemories;
	private Map<FunctionDecl, Function> declToFunction;
	private Map<TableDecl, Table> declToTable;
	private Map<MemoryDecl, Memory> declToMemory;
	private Map<String, Export> exports;

	public DefaultInstance(WasmModule module, Importer importer) {
		this.module = module;
		this.allFunctions = new ArrayList<>();
		this.allTables = new ArrayList<>();
		this.allMemories = new ArrayList<>();
		this.declToFunction = new HashMap<>();
		this.declToTable = new HashMap<>();
		this.declToMemory = new HashMap<>();
		this.exports = new HashMap<>();
		setup(importer);
	}

	private void setup(Importer importer) {
		if (module.declaredImports().size() > 0 && importer == null) {
			throw new IllegalArgumentException("The module have at least 1 import; an importer must be provided.");
		}

		// TODO initialize everything

		for (TableDecl decl : module.declaredTables()) {
			Table table;

			if (decl instanceof ModuleTableDecl moduleDecl) {
				table = new DefaultTable(moduleDecl);
			} else if (decl instanceof ImportTableDecl imp) {
				String mod = imp.declaration().module();
				String name = imp.declaration().name();
				table = importer.importTable(mod, name);
				if (table == null) throw new IllegalArgumentException("Module requires table %s::%s"
					.formatted(mod, name));
				if (table.refType() != imp.type().refType())
					throw new IllegalArgumentException("Imported table %s::%s type mismatch: %s (decl) != %s"
						.formatted(mod, name, imp.type().refType(), table.refType()));
				if (table.size() < imp.type().limit().min())
					throw new IllegalArgumentException("Imported table %s::%s does not have required size: %s"
						.formatted(mod, name, imp.type().limit()));
				if (table.declaration() != null) declToTable.put(table.declaration(), table);
			} else {
				throw new IllegalArgumentException("Unable to resolve table declaration for %s"
					.formatted(decl.getClass().getName()));
			}

			allTables.add(table);
			declToTable.put(decl, table);
		}

		for (MemoryDecl decl : module.declaredMemories()) {
			Memory memory;

			if (decl instanceof ModuleMemoryDecl moduleDecl) {
				memory = new DefaultMemory(moduleDecl);
			} else if (decl instanceof ImportMemoryDecl imp) {
				String mod = imp.declaration().module();
				String name = imp.declaration().name();
				memory = importer.importMemory(mod, name);
				if (memory == null) throw new IllegalArgumentException("Module requires memory %s::%s"
					.formatted(mod, name));
				if (memory.pageCount() < imp.type().limit().min())
					throw new IllegalArgumentException("Imported memory %s::%s does not have required size: %s"
						.formatted(mod, name, imp.type().limit()));
				if (memory.declaration() != null) declToMemory.put(memory.declaration(), memory);
			} else {
				throw new IllegalArgumentException("Unable to resolve memory declaration for %s"
					.formatted(decl.getClass().getName()));
			}

			allMemories.add(memory);
			declToMemory.put(decl, memory);
		}

		for (FunctionDecl decl : module.declaredFunctions()) {
			Function function;

			if (decl instanceof ImportFunctionDecl imp) {
				String mod = imp.declaration().module();
				String name = imp.declaration().name();
				FunctionType type = imp.type();
				function = importer.importFunction(mod, name);
				if (function == null) throw new IllegalArgumentException("Module requires function %s::%s"
					.formatted(mod, name));
				if (!type.equals(function.type()))
					throw new IllegalArgumentException("Imported function %s::%s type mismatch: %s (decl) != %s"
						.formatted(mod, name, type, function.type()));

				// Function always have foreign declaration
				declToFunction.put(function.declaration(), function);
			} else {
				function = new Function(this, decl);
			}

			allFunctions.add(function);
			declToFunction.put(decl, function);
		}

		for (ExportDecl export : module.declaredExports()) {
			Exportable exportable;

			if (export.description() instanceof FunctionExportDescription desc) {
				exportable = declToFunction.get(desc.function());
			} else if (export.description() instanceof TableExportDescription desc) {
				exportable = declToTable.get(desc.table());
			} else if (export.description() instanceof MemoryExportDescription desc) {
				exportable = declToMemory.get(desc.memory());
			} else {
				throw new IllegalArgumentException("Unable to resolve export declaration for %s"
					.formatted(export.description().getClass().getName()));
			}

			exports.put(export.name(), new Export(export, exportable));
		}
	}

	@Override
	public WasmModule module() {
		return module;
	}

	@Override
	public List<Function> functions() {
		return allFunctions;
	}

	@Override
	public Function function(FunctionDecl decl) {
		return declToFunction.get(decl);
	}

	@Override
	public List<Table> tables() {
		return allTables;
	}

	@Override
	public Table table(TableDecl decl) {
		return declToTable.get(decl);
	}

	@Override
	public List<Memory> memories() {
		return allMemories;
	}

	@Override
	public Memory memory(MemoryDecl decl) {
		return declToMemory.get(decl);
	}

	@Override
	public Collection<Export> exports() {
		return exports.values();
	}

	@Override
	public Export export(String name) {
		return exports.get(name);
	}
}
