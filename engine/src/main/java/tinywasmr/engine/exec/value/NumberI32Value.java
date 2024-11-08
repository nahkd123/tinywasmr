package tinywasmr.engine.exec.value;

import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.ValueType;

public record NumberI32Value(int i32) implements Value {
	@Override
	public ValueType type() {
		return NumberType.I32;
	}

	@Override
	public boolean condition() {
		return i32 != 0;
	}

	@Override
	public long i64() {
		return i32;
	}

	@Override
	public float f32() {
		return i32;
	}

	@Override
	public double f64() {
		return i32;
	}
}
