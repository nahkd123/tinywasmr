package tinywasmr.engine.module.v1.importing;

import tinywasmr.engine.module.exception.LinkingException;
import tinywasmr.engine.module.importing.FunctionImport;
import tinywasmr.engine.module.type.FunctionType;
import tinywasmr.engine.module.v1.WasmModuleImpl;

public class FunctionImportImpl implements FunctionImport {
	private String moduleName;
	private String importName;
	private int typeIndex;
	private FunctionType linkedType;

	public FunctionImportImpl(String moduleName, String importName, int typeIndex) {
		this.moduleName = moduleName;
		this.importName = importName;
		this.typeIndex = typeIndex;
	}

	public void link(WasmModuleImpl module) {
		var typesSection = module.getTypesSection();
		if (typesSection.isEmpty()) throw new LinkingException("Module does not have types section");

		var types = typesSection.get().getTypes();
		if (typeIndex >= types.size()) throw new LinkingException("Invalid import type index: " + typeIndex);

		var type = types.get(typeIndex);
		if (!(type instanceof FunctionType func))
			throw new LinkingException("Type is not function signature: " + type + " @ " + typeIndex + "");

		linkedType = func;
	}

	@Override
	public String getModuleName() { return moduleName; }

	@Override
	public String getImportName() { return importName; }

	@Override
	public FunctionType getFunctionSignature() {
		if (linkedType == null) throw new IllegalStateException("Not linked yet");
		return linkedType;
	}

	@Override
	public String toString() {
		return moduleName + "/" + importName + ": "
			+ (linkedType == null ? ("<not linked (" + typeIndex + ")>") : linkedType);
	}
}
