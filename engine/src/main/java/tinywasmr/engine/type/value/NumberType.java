package tinywasmr.engine.type.value;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.ValidationException;
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

	@Override
	public Value mapFromJava(Object object) {
		if (!(object instanceof Number num)) throw new IllegalArgumentException("Not a number");
		return switch (this) {
		case I32 -> new NumberI32Value(num.intValue());
		case I64 -> new NumberI64Value(num.longValue());
		case F32 -> new NumberF32Value(num.floatValue());
		case F64 -> new NumberF64Value(num.doubleValue());
		};
	}

	@Override
	public Number mapToJava(Value value) {
		if (value.type() != this)
			throw new ValidationException("Type mismatch: %s (value) != %s (type)".formatted(value.type(), this));
		if (value instanceof NumberI32Value i32) return i32.i32();
		if (value instanceof NumberI64Value i64) return i64.i64();
		if (value instanceof NumberF32Value f32) return f32.f32();
		if (value instanceof NumberF64Value f64) return f64.f64();
		throw new RuntimeException("Unreachable");
	}
}
