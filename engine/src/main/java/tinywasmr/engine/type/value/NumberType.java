package tinywasmr.engine.type.value;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.value.NumberF32Value;
import tinywasmr.engine.exec.value.NumberF64Value;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.value.NumberI64Value;
import tinywasmr.engine.exec.value.Value;

public enum NumberType implements ValueType {
	I32,
	I64,
	F32,
	F64;

	@Override
	public List<ValueType> blockResults() {
		return Collections.singletonList(this);
	}

	@Override
	public Value zero() {
		return switch (this) {
		case I32 -> new NumberI32Value(0);
		case I64 -> new NumberI64Value(0);
		case F32 -> new NumberF32Value(0);
		case F64 -> new NumberF64Value(0);
		};
	}
}
