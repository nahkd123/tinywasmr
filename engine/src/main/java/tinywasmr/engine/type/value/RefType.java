package tinywasmr.engine.type.value;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.value.RefValue;

public enum RefType implements ValueType {
	FUNC(RefValue.NULL_FUNC),
	EXTERN(RefValue.NULL_EXTERN);

	private RefValue zero;

	private RefType(RefValue zero) {
		this.zero = zero;
	}

	@Override
	public List<ValueType> blockResults() {
		return Collections.singletonList(this);
	}

	@Override
	public RefValue zero() {
		return zero;
	}
}
