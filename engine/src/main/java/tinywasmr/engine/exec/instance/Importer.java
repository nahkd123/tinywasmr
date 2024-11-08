package tinywasmr.engine.exec.instance;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.table.Table;

public interface Importer {
	Function importFunction(String module, String name);

	Table importTable(String module, String name);

	Memory importMemory(String module, String name);
}
