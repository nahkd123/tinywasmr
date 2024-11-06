package tinywasmr.engine.util;

/**
 * <p>
 * An abstraction for loggers. Is not used by engine, but by other components,
 * like trace for example.
 * </p>
 */
public interface Logger {
	public void verbose(String format, Object... values);

	public void info(String format, Object... values);

	public void warn(String format, Object... values);

	public void error(String format, Object... values);
};
