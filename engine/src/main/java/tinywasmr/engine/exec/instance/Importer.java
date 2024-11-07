package tinywasmr.engine.exec.instance;

import tinywasmr.engine.exec.table.Table;

public interface Importer {
	Function importFunction(String module, String name);

	Table importTable(String module, String name);
}
