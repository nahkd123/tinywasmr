package tinywasmr.engine.io;

import java.io.IOException;

@FunctionalInterface
public interface IOConsumer<T> {
	public void consume(T obj) throws IOException;
}
