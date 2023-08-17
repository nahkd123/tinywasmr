package tinywasmr.engine.module.importing;

import tinywasmr.engine.module.type.FunctionType;

public interface FunctionImport extends Import {
	@Override
	default ImportDesc getImportDescType() { return ImportDesc.FUNCTION; }

	public FunctionType getFunctionSignature();
}
