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

	// Vector operations
	public Vector128Value not() {
		return new Vector128Value(~msb, ~lsb);
	}

	public static Vector128Value and(Vector128Value a, Vector128Value b) {
		return new Vector128Value(a.msb & b.msb, a.lsb & b.lsb);
	}

	public static Vector128Value andnot(Vector128Value a, Vector128Value b) {
		return new Vector128Value(a.msb & (~b.msb), a.lsb & (~b.lsb));
	}

	public static Vector128Value or(Vector128Value a, Vector128Value b) {
		return new Vector128Value(a.msb | b.msb, a.lsb | b.lsb);
	}

	public static Vector128Value xor(Vector128Value a, Vector128Value b) {
		return new Vector128Value(a.msb ^ b.msb, a.lsb ^ b.lsb);
	}

	public static Vector128Value bitselect(Vector128Value a, Vector128Value b, Vector128Value c) {
		return new Vector128Value((a.msb & c.msb) | (b.msb & (~c.msb)), (a.lsb & c.lsb) | (b.lsb & (~c.lsb)));
	}
}
