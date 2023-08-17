package tinywasmr.engine.module.v1.function;

import tinywasmr.engine.module.exception.LinkingException;
import tinywasmr.engine.module.function.Function;
import tinywasmr.engine.module.function.FunctionCode;
import tinywasmr.engine.module.type.FunctionType;
import tinywasmr.engine.module.v1.WasmModuleImpl;

public class FunctionImpl implements Function {
	private int typeIndex;
	private FunctionType linkedType;
	private FunctionCode linkedCode;

	public FunctionImpl(int typeIndex) {
		this.typeIndex = typeIndex;
	}

	public void link(WasmModuleImpl module) {
		if (linkedType != null) return;
		var typesSection = module.getTypesSection();
		if (typesSection.isEmpty()) throw new LinkingException("Module does not have types section");

		var codeSection = module.getCodeSection();
		if (codeSection.isEmpty()) throw new LinkingException("Module does not have code section");

		var types = typesSection.get().getTypes();
		if (typeIndex >= types.size()) throw new LinkingException("Invalid import type index: " + typeIndex);

		var type = types.get(typeIndex);
		if (!(type instanceof FunctionType func))
			throw new LinkingException("Type is not function signature: " + type + " @ " + typeIndex + "");

		linkedType = func;
		linkedCode = codeSection.get()
			.getFunctions()
			.get(typeIndex - module.getImportsSection().map(v -> v.getImports().size()).orElse(0));
	}

	@Override
	public FunctionType getSignature() {
		if (linkedType == null) throw new IllegalStateException("Not linked yet");
		return linkedType;
	}

	@Override
	public FunctionCode getCode() {
		if (linkedCode == null) throw new IllegalStateException("Not linked yet");
		return linkedCode;
	}

	@Override
	public String toString() {
		return linkedType == null ? ("<not linked (" + typeIndex + ")>") : linkedType.toString();
	}
}
