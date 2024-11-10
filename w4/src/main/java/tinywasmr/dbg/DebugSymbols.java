package tinywasmr.dbg;

import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.global.GlobalDecl;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.table.TableDecl;

/**
 * <p>
 * Resolving the name of symbols, which will be displayed in debugger.
 * </p>
 */
public interface DebugSymbols {
	String nameOf(WasmModule module);

	default String nameOf(Instance instance) {
		if (instance.module() == null) return "external instance";
		return "instance of %s".formatted(nameOf(instance.module()));
	}

	String nameOf(FunctionDecl function);

	String nameOf(TableDecl table);

	String nameOf(MemoryDecl memory);

	String nameOf(GlobalDecl global);
}
