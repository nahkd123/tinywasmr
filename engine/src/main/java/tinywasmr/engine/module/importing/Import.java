package tinywasmr.engine.module.importing;

public interface Import {
	public String getModuleName();

	public String getImportName();

	public ImportDesc getImportDescType();
}
