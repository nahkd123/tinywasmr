package tinywasmr.engine.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class LEDataInputStream implements LEDataInput, Closeable {
	private InputStream stream;

	public LEDataInputStream(InputStream stream) {
		this.stream = stream;
	}

	@Override
	public int readByte() throws IOException {
		return stream.read();
	}

	@Override
	public int readBytesTo(byte[] buffer, int offsetInBuffer, int length) throws IOException {
		return stream.read(buffer, offsetInBuffer, length);
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}
}
