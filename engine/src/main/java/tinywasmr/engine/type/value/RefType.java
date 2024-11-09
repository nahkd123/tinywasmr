package tinywasmr.engine.type.value;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.value.ExternRefValue;
import tinywasmr.engine.exec.value.FuncRefValue;
import tinywasmr.engine.exec.value.RefValue;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.module.func.extern.HostOnlyFunctionDecl;

public enum RefType implements ValueType {
	FUNC(RefValue.NULL_FUNC),
	EXTERN(RefValue.NULL_EXTERN);

	private RefValue zero;

	private RefType(RefValue zero) {
		this.zero = zero;
	}

	@Override
	public List<ValueType> blockResults() {
		return Collections.singletonList(this);
	}

	@Override
	public RefValue zero() {
		return zero;
	}

	@Override
	public RefValue mapFromJava(Object object) {
		if (this == EXTERN) {
			if (object == null) return RefValue.NULL_EXTERN;
			return new ExternRefValue(object);
		}

		if (this == FUNC) {
			if (object == null) return RefValue.NULL_FUNC;
			if (object instanceof Function function) return new FuncRefValue(function);
			if (object instanceof Runnable runnable)
				return new FuncRefValue(new Function(null, HostOnlyFunctionDecl.ofVoid(runnable)));
			// Not possible to derive parameters and result types from Consumer, BiConsumer,
			// Function and BiFunction.
		}

		throw new IllegalArgumentException("Unable to convert %s to %s".formatted(object, this));
	}

	@Override
	public Object mapToJava(Value value) {
		if (value.type() != this)
			throw new ValidationException("Type mismatch: %s (value) != %s (type)".formatted(value.type(), this));

		if (this == EXTERN) {
			if (!(value instanceof ExternRefValue extern)) throw new IllegalArgumentException("Value is not externref");
			return extern.value();
		}

		if (this == FUNC) {
			if (!(value instanceof FuncRefValue func)) throw new IllegalArgumentException("Value is not funcref");
			return func.function();
		}

		throw new IllegalArgumentException("Unable to convert %s to %s".formatted(value, this));
	}
}
