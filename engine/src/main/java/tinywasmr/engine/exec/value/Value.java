package tinywasmr.engine.exec.value;

import tinywasmr.engine.type.value.ValueType;

public interface Value {
	ValueType type();

	boolean condition();

	int i32();

	long i64();

	float f32();

	double f64();

	Object mapToJava();

	static Value mapFromJava(Object java) {
		if (java instanceof Integer i32) return new NumberI32Value(i32);
		if (java instanceof Long i64) return new NumberI64Value(i64);
		if (java instanceof Float f32) return new NumberF32Value(f32);
		if (java instanceof Double f64) return new NumberF64Value(f64);
		if (java instanceof long[] v128) return new Vector128Value(v128[0], v128[1]);
		throw new IllegalArgumentException("Unknown type: %s".formatted(java.getClass().getName()));
	}
}
