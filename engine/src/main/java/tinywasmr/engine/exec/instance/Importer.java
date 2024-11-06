package tinywasmr.engine.exec.instance;

public interface Importer {
	Function importFunction(String module, String name);
}
