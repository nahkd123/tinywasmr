package tinywasmr.engine.type.value;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.ValidationException;
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

	@Override
	public Value mapFromJava(Object object) {
		if (object instanceof long[] arr && arr.length == 2) return new Vector128Value(arr[0], arr[1]);
		// TODO add BigInteger conversion
		throw new IllegalArgumentException("Unable to convert %s to v128".formatted(object, this));
	}

	@Override
	public Object mapToJava(Value value) {
		if (value.type() != this)
			throw new ValidationException("Type mismatch: %s (value) != %s (type)".formatted(value.type(), this));
		if (value instanceof Vector128Value v128) return new long[] { v128.msb(), v128.lsb() };
		throw new IllegalArgumentException("Unable to convert %s to long[2]".formatted(value, this));
	}
}
