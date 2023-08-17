package tinywasmr.engine.type;

public class Limit {
	private long min;
	private long max;

	private Limit(long min, long max) {
		this.min = min;
		this.max = max;
	}

	public static Limit min(long min) {
		if (min < 0) throw new IllegalArgumentException("min must be greater than or equals to 0");
		return new Limit(min, -1);
	}

	public static Limit max(long min, long max) {
		if (min < 0) throw new IllegalArgumentException("min must be greater than or equals to 0");
		if (max < min) throw new IllegalArgumentException("max must be greater than or equals to min");
		return new Limit(min, max);
	}

	public long getMin() { return min; }

	public boolean hasMax() {
		return max != -1;
	}

	public long getMax() {
		if (max == -1) throw new IllegalStateException("Limit does not have max value (check hasMax())");
		return max;
	}

	@Override
	public String toString() {
		return "Limit(" + min + (max == -1 ? " minimum" : (" -> " + max)) + ")";
	}
}
