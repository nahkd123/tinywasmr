package tinywasmr.engine.type;

public record Limit(int min, int max) {
	public Limit {
		if (max < -1) throw new IllegalArgumentException("Expecting -1 or non-negative value for max, but %d found"
			.formatted(max));
	}

	/**
	 * <p>
	 * Create a new limit without maximum value.
	 * </p>
	 * 
	 * @param min The minimum value of the limit.
	 */
	public Limit(int min) {
		this(min, -1);
	}

	/**
	 * <p>
	 * Check whether this limit have maximum value.
	 * </p>
	 * 
	 * @return {@code true} if there is maximum value.
	 */
	public final boolean hasMax() {
		return max != -1;
	}

	/**
	 * <p>
	 * Get the maximum from this limit. This method throws
	 * {@link IllegalArgumentException} if {@link #hasMax()} returns false.
	 * </p>
	 * 
	 * @return The maximum value.
	 * @throws IllegalArgumentException when {@link #hasMax()} is false.
	 */
	@Override
	public final int max() {
		if (max == -1) throw new IllegalArgumentException("This limit does not have maximum value (hasMax() == false)");
		return max;
	}

	public final int allocSize() {
		return max == -1 ? min : max;
	}

	@Override
	public final String toString() {
		if (max == -1) return "minimum of %d".formatted(min);
		return "from %d to %d".formatted(min, max);
	}
}
