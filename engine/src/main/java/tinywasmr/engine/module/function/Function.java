package tinywasmr.engine.module.function;

import tinywasmr.engine.module.type.FunctionType;

public interface Function {
	public FunctionType getSignature();

	public FunctionCode getCode();
}
