package tinywasmr.engine.exec.value;

import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.ValueType;

public record NumberF32Value(float f32) implements Value {
	@Override
	public ValueType type() {
		return NumberType.F32;
	}

	@Override
	public boolean condition() {
		return f32 != 0f;
	}

	@Override
	public int i32() {
		return (int) f32;
	}

	@Override
	public long i64() {
		return (long) f32;
	}

	@Override
	public double f64() {
		return f32;
	}

	@Override
	public final String toString() {
		return "f32 %f".formatted(f32);
	}
}
