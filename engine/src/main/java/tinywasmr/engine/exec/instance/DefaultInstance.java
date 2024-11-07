package tinywasmr.engine.exec.instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.export.FunctionExportDescription;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.ImportFunctionDecl;
import tinywasmr.engine.type.FunctionType;

public class DefaultInstance implements Instance {
	private WasmModule module;
	private List<Function> allFunctions;
	private Map<FunctionDecl, Function> declToFunction;
	private Map<String, Export> exports;

	public DefaultInstance(WasmModule module, Importer importer) {
		this.module = module;
		this.allFunctions = new ArrayList<>();
		this.declToFunction = new HashMap<>();
		this.exports = new HashMap<>();
		setup(importer);
	}

	private void setup(Importer importer) {
		if (module.declaredImports().size() > 0 && importer == null) {
			throw new IllegalArgumentException("The module have at least 1 import; an importer must be provided.");
		}

		// TODO initialize everything

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
			} else {
				function = new Function(this, decl);
			}

			allFunctions.add(function);
			declToFunction.put(decl, function);
		}

		for (ExportDecl export : module.declaredExports()) {
			if (export.description() instanceof FunctionExportDescription desc) {
				FunctionDecl decl = desc.function();
				Function function = declToFunction.get(decl);
				exports.put(export.name(), new Export(export, function));
			}
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
	public Collection<Export> exports() {
		return exports.values();
	}

	@Override
	public Export export(String name) {
		return exports.get(name);
	}
}
