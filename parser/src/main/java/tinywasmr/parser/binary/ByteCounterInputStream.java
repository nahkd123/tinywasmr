package tinywasmr.parser.binary;

import java.io.IOException;
import java.io.InputStream;

class ByteCounterInputStream extends InputStream {
	private InputStream proxyOf;
	private int counter = 0;

	public ByteCounterInputStream(InputStream proxyOf) {
		this.proxyOf = proxyOf;
	}

	public int getCounter() { return counter; }

	@Override
	public int read() throws IOException {
		int v = proxyOf.read();
		if (v != -1) counter++;
		return v;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int i = super.read(b, off, len);
		counter += i;
		return i;
	}

	@Override
	public void close() throws IOException {
		proxyOf.close();
	}

	@Override
	public int available() throws IOException {
		return proxyOf.available();
	}
}
