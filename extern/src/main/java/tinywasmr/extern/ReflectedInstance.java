package tinywasmr.extern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinywasmr.engine.exec.global.Global;
import tinywasmr.engine.exec.instance.Export;
import tinywasmr.engine.exec.instance.Exportable;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.export.FunctionExportDescription;
import tinywasmr.engine.module.export.MemoryExportDescription;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.memory.MemoryDecl;

public class ReflectedInstance<T> implements Instance {
	private ReflectedModule<T> module;
	private T object;

	private List<Function> functions = new ArrayList<>();
	private List<Memory> memories = new ArrayList<>();
	private Map<FunctionDecl, Function> declToFunctions = new HashMap<>();
	private Map<MemoryDecl, Memory> declToMemories = new HashMap<>();
	private Map<String, Export> exports = new HashMap<>();

	public ReflectedInstance(ReflectedModule<T> module, T object) {
		this.module = module;
		this.object = object;

		for (FunctionDecl decl : module.declaredFunctions()) {
			Function function = new Function(this, decl);
			functions.add(function);
			declToFunctions.put(decl, function);
		}

		for (MemoryDecl decl : module.declaredMemories()) {
			if (!(decl instanceof ReflectedMemoryDecl reflected)) throw new IllegalArgumentException("Not reflected");
			Memory memory = reflected.getFrom(object);
			memories.add(memory);
			declToMemories.put(reflected, memory);
		}

		for (ExportDecl decl : module.declaredExports()) {
			Exportable exp = null;
			if (decl.description() instanceof FunctionExportDescription d) exp = function(d.function());
			if (decl.description() instanceof MemoryExportDescription d) exp = memory(d.memory());
			if (exp == null) throw new RuntimeException("Not implemented: %s".formatted(exp));
			exports.put(decl.name(), new Export(decl, exp));
		}
	}

	public T object() {
		return object;
	}

	@Override
	public ReflectedModule<T> module() {
		return module;
	}

	@Override
	public List<Function> functions() {
		return functions;
	}

	@Override
	public Function function(FunctionDecl decl) {
		return declToFunctions.get(decl);
	}

	@Override
	public List<Table> tables() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public List<Global> globals() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public List<Memory> memories() {
		return memories;
	}

	@Override
	public Memory memory(MemoryDecl decl) {
		return declToMemories.get(decl);
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
