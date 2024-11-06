package tinywasmr.parser.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

record BinaryExport(String name, int type, int index) {

	public static final int TYPE_FUNC = 0x00;
	public static final int TYPE_TABLE = 0x01;
	public static final int TYPE_MEM = 0x02;
	public static final int TYPE_GLOBAL = 0x03;

	public static BinaryExport parse(InputStream stream) throws IOException {
		String name = StreamReader.readName(stream);
		int type = stream.read();
		if (type == -1) throw new EOFException();
		int index = StreamReader.readUint32Var(stream);
		return new BinaryExport(name, type, index);
	}
}
