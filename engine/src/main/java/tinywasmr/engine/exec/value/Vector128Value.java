package tinywasmr.engine.exec.value;

import tinywasmr.engine.type.value.ValueType;
import tinywasmr.engine.type.value.VectorType;

public record Vector128Value(long msb, long lsb) implements Value {
	public static final Vector128Value ZERO = new Vector128Value(0, 0);

	@Override
	public ValueType type() {
		return VectorType.V128;
	}

	@Override
	public boolean condition() {
		return msb != 0L && lsb != 0L;
	}

	@Override
	public int i32() {
		return (int) lsb;
	}

	@Override
	public long i64() {
		return lsb;
	}

	@Override
	public float f32() {
		return lsb;
	}

	@Override
	public double f64() {
		return lsb;
	}
}
