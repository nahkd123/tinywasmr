package tinywasmr.engine.exec.value;

import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.type.value.RefType;
import tinywasmr.engine.type.value.ValueType;

public record FuncRefValue(Function function) implements RefValue {
	@Override
	public Object get() {
		return function;
	}

	@Override
	public ValueType type() {
		return RefType.FUNC;
	}
}
