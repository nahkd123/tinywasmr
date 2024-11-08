package tinywasmr.engine.exec.value;

import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.ValueType;

public record NumberF64Value(double f64) implements Value {
	@Override
	public ValueType type() {
		return NumberType.F64;
	}

	@Override
	public boolean condition() {
		return f64 != 0d;
	}

	@Override
	public int i32() {
		return (int) f64;
	}

	@Override
	public long i64() {
		return (long) f64;
	}

	@Override
	public float f32() {
		return (float) f64;
	}
}
