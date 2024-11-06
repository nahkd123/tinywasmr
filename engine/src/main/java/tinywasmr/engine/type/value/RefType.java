package tinywasmr.engine.type.value;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.value.Value;

public enum RefType implements ValueType {
	FUNC,
	EXTERN;

	@Override
	public List<ValueType> blockResults() {
		return Collections.singletonList(this);
	}

	@Override
	public Value zero() {
		throw new RuntimeException("Not implemented");
	}
}
