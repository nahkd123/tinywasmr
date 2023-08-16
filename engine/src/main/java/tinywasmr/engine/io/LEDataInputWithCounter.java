package tinywasmr.engine.io;

import java.io.IOException;

import tinywasmr.engine.util.Tuple;

public class LEDataInputWithCounter implements LEDataInput {
	private LEDataInput backed;
	private int totalBytesRead = 0;

	public LEDataInputWithCounter(LEDataInput backed) {
		this.backed = backed;
	}

	public LEDataInput getBacked() { return backed; }

	public int getTotalBytesRead() { return totalBytesRead; }

	@Override
	public int readByte() throws IOException {
		int v = backed.readByte();
		if (v == -1) return -1;
		totalBytesRead++;
		return v;
	}

	public static <T> Tuple.Duo<Integer, T> countBytes(LEDataInput stream, IOFunction<LEDataInput, T> reader) throws IOException {
		var counter = new LEDataInputWithCounter(stream);
		var out = reader.apply(counter);
		return new Tuple.Duo<Integer, T>(counter.totalBytesRead, out);
	}
}
