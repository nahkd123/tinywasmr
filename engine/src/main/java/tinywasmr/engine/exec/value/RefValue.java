package tinywasmr.engine.exec.value;

public interface RefValue extends Value {
	/**
	 * <p>
	 * Unwrap to value in Java. Returns {@code null} if this reference value is
	 * {@code null}.
	 * </p>
	 */
	Object get();

	@Override
	default boolean condition() {
		return get() != null;
	}

	@Override
	default int i32() {
		throw new RuntimeException("ref cannot be unwrapped into i32");
	}

	@Override
	default long i64() {
		throw new RuntimeException("ref cannot be unwrapped into i64");
	}

	@Override
	default float f32() {
		throw new RuntimeException("ref cannot be unwrapped into f32");
	}

	@Override
	default double f64() {
		throw new RuntimeException("ref cannot be unwrapped into f64");
	}

	static FuncRefValue NULL_FUNC = new FuncRefValue(null);
	static ExternRefValue NULL_EXTERN = new ExternRefValue(null);
}
