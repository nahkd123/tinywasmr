package tinywasmr.parser.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public record SectionHeader(int id, int size) {
	public static SectionHeader parse(InputStream stream) throws IOException {
		int id = stream.read();
		if (id == -1) throw new EOFException();
		int size = StreamReader.readUint32Var(stream);
		return new SectionHeader(id, size);
	}
}
