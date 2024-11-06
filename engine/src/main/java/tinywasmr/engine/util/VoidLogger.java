package tinywasmr.engine.util;

public class VoidLogger implements Logger {
	@Override
	public void verbose(String format, Object... values) {}

	@Override
	public void info(String format, Object... values) {}

	@Override
	public void warn(String format, Object... values) {}

	@Override
	public void error(String format, Object... values) {}
}
