package tinywasmr.engine.exec.value;

import tinywasmr.engine.type.value.ValueType;

public interface Value {
	ValueType type();

	boolean condition();

	int i32();

	long i64();

	float f32();

	double f64();

	default Object mapToJava() {
		return type().mapToJava(this);
	}

	static Value TRUE = new NumberI32Value(1);
	static Value FALSE = new NumberI32Value(0);
}
