package tinywasmr.engine.module;

import tinywasmr.engine.module.type.FunctionType;

public interface Function {
	public FunctionType getSignature();

	// TODO add call method here when we reach to vm part
}
