package tinywasmr.extern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinywasmr.engine.exec.instance.Export;
import tinywasmr.engine.exec.instance.Exportable;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.export.FunctionExportDescription;
import tinywasmr.engine.module.func.FunctionDecl;

public class ReflectedInstance<T> implements Instance {
	private ReflectedWasmModule<T> module;
	private T object;

	private List<Function> functions = new ArrayList<>();
	private Map<FunctionDecl, Function> declToFunctions = new HashMap<>();
	private Map<String, Export> exports = new HashMap<>();

	public ReflectedInstance(ReflectedWasmModule<T> module, T object) {
		this.module = module;
		this.object = object;

		for (FunctionDecl decl : module.declaredFunctions()) {
			Function function = new Function(this, decl);
			functions.add(function);
			declToFunctions.put(decl, function);
		}

		for (ExportDecl decl : module.declaredExports()) {
			Exportable exp = null;
			if (decl.description() instanceof FunctionExportDescription d) exp = function(d.function());
			if (exp == null) throw new RuntimeException("Not implemented: %s".formatted(exp));
			exports.put(decl.name(), new Export(decl, exp));
		}
	}

	public T object() {
		return object;
	}

	@Override
	public ReflectedWasmModule<T> module() {
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
	public List<Memory> memories() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
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
