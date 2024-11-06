package tinywasmr.engine.type.value;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.value.Vector128Value;

public enum VectorType implements ValueType {
	V128;

	@Override
	public List<ValueType> blockResults() {
		return Collections.singletonList(this);
	}

	@Override
	public Value zero() {
		return Vector128Value.ZERO;
	}
}
