package tinywasmr.dbg;

import java.util.HashMap;
import java.util.Map;

import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.export.FunctionExportDescription;
import tinywasmr.engine.module.export.MemoryExportDescription;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.ModuleFunctionDecl;
import tinywasmr.engine.module.global.GlobalDecl;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.memory.ModuleMemoryDecl;
import tinywasmr.engine.module.table.TableDecl;
import tinywasmr.extern.ReflectedFunctionDecl;
import tinywasmr.extern.ReflectedMemoryDecl;
import tinywasmr.extern.ReflectedModule;

public class AutoDebugSymbols implements DebugSymbols {
	private Map<Object, String> names = new HashMap<>();
	private int moduleId = 0, tableId = 0, memoryId = 0, globalId = 0, functionId = 0;

	public void setName(Object symbol, String name) {
		names.put(symbol, name);
	}

	@Override
	public String nameOf(WasmModule module) {
		return names.computeIfAbsent(module, m -> {
			if (module instanceof ReflectedModule<?> refl) return "extern[%s]".formatted(refl.clazz().getName());
			return "module%04d".formatted(moduleId++);
		});
	}

	@Override
	public String nameOf(FunctionDecl function) {
		return names.computeIfAbsent(function, f -> {
			if (function instanceof ReflectedFunctionDecl decl) return decl.method().getName();

			if (function instanceof ModuleFunctionDecl decl) {
				WasmModule mod = decl.module();
				String modName = nameOf(mod);
				String exportedName = mod.declaredExports().stream()
					.filter(e -> e.description() instanceof FunctionExportDescription desc && desc.function() == decl)
					.findAny()
					.map(e -> e.name())
					.orElseGet(() -> "func%04d".formatted(functionId++));
				return "%s::%s".formatted(modName, exportedName);
			}

			return "func%04d".formatted(functionId++);
		});
	}

	@Override
	public String nameOf(TableDecl table) {
		return names.computeIfAbsent(table, t -> {
			return "table%04d".formatted(tableId++);
		});
	}

	@Override
	public String nameOf(MemoryDecl memory) {
		return names.computeIfAbsent(memory, m -> {
			if (memory instanceof ReflectedMemoryDecl decl) return decl.field().getName();

			if (memory instanceof ModuleMemoryDecl decl) {
				WasmModule mod = decl.module();
				String modName = nameOf(mod);
				String exportedName = mod.declaredExports().stream()
					.filter(e -> e.description() instanceof MemoryExportDescription desc && desc.memory() == decl)
					.findAny()
					.map(e -> e.name())
					.orElseGet(() -> "memory%04d".formatted(memoryId++));
				return "%s::%s".formatted(modName, exportedName);
			}

			return "memory%04d".formatted(memoryId++);
		});
	}

	@Override
	public String nameOf(GlobalDecl global) {
		return names.computeIfAbsent(global, g -> {
			return "global%04d".formatted(globalId++);
		});
	}
}
