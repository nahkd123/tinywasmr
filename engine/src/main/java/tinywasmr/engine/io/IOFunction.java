package tinywasmr.engine.io;

import java.io.IOException;

@FunctionalInterface
public interface IOFunction<T, R> {
	public R apply(T in) throws IOException;
}
