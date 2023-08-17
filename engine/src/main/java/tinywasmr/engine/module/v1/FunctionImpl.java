package tinywasmr.engine.module.v1;

import tinywasmr.engine.module.Function;
import tinywasmr.engine.module.exception.LinkingException;
import tinywasmr.engine.module.type.FunctionType;

public class FunctionImpl implements Function {
	private int typeIndex;
	private FunctionType linkedType;

	public FunctionImpl(int typeIndex) {
		this.typeIndex = typeIndex;
	}

	public void link(WasmModuleImpl module) {
		if (linkedType != null) return;
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
	public FunctionType getSignature() {
		if (linkedType == null) throw new IllegalStateException("Not linked yet");
		return linkedType;
	}

	@Override
	public String toString() {
		return linkedType == null ? ("<not linked (" + typeIndex + ")>") : linkedType.toString();
	}
}
