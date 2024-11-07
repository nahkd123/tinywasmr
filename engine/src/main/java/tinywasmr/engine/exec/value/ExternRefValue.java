package tinywasmr.engine.exec.value;

import tinywasmr.engine.type.value.RefType;
import tinywasmr.engine.type.value.ValueType;

public record ExternRefValue(Object value) implements RefValue {
	@Override
	public Object get() {
		return value;
	}

	@Override
	public ValueType type() {
		return RefType.EXTERN;
	}
}
