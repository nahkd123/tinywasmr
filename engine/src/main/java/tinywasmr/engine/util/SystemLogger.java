package tinywasmr.engine.util;

public class SystemLogger implements Logger {
	private boolean verbose;

	public SystemLogger(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public void verbose(String format, Object... values) {
		if (!verbose) return;
		System.err.println("\u001b[90m[verbose] \u001b[3m" + format.formatted(values) + "\u001b[0m");
	}

	@Override
	public void info(String format, Object... values) {
		System.err.println("\u001b[36m[info] " + format.formatted(values));
	}

	@Override
	public void warn(String format, Object... values) {
		System.err.println("\u001b[33m[warn] " + format.formatted(values));
	}

	@Override
	public void error(String format, Object... values) {
		System.err.println("\u001b[91m[ERROR] " + format.formatted(values));
	}
}
