package tinywasmr.engine.module.v1.type;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.module.type.FunctionType;
import tinywasmr.engine.module.type.Type;

public class FunctionTypeImpl implements FunctionType {
	private List<Type> argumentTypes;
	private List<Type> returnTypes;

	public FunctionTypeImpl(List<Type> argumentTypes, List<Type> returnTypes) {
		this.argumentTypes = argumentTypes;
		this.returnTypes = returnTypes;
	}

	@Override
	public List<Type> getArgumentTypes() { return Collections.unmodifiableList(argumentTypes); }

	@Override
	public List<Type> getReturnTypes() { return Collections.unmodifiableList(returnTypes); }

	@Override
	public String toString() {
		var args = String.join(", ", argumentTypes.stream().map(v -> v.toString()).toArray(String[]::new));
		var returns = String.join(", ", returnTypes.stream().map(v -> v.toString()).toArray(String[]::new));
		return "function (" + args + ") -> (" + returns + ")";
	}
}
