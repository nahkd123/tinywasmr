package tinywasmr.engine.exec.value;

import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.ValueType;

public record NumberI64Value(long i64) implements Value {
	@Override
	public ValueType type() {
		return NumberType.I64;
	}

	@Override
	public boolean condition() {
		return i64 != 0L;
	}

	@Override
	public int i32() {
		return (int) i64;
	}

	@Override
	public float f32() {
		return i64;
	}

	@Override
	public double f64() {
		return i64;
	}

	@Override
	public final String toString() {
		return "i64 ".formatted(i64);
	}
}
