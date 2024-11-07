package tinywasmr.parser.binary.imprt;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import tinywasmr.parser.binary.BinaryModuleParser;
import tinywasmr.parser.binary.StreamReader;

public record BinaryImport(String module, String name, BinaryImportDesc description) {

	public static final int TYPE_FUNC = 0x00;
	public static final int TYPE_TABLE = 0x01;
	public static final int TYPE_MEM = 0x02;
	public static final int TYPE_GLOBAL = 0x03;

	public static BinaryImport parse(BinaryModuleParser moduleParser, InputStream stream) throws IOException {
		String module = StreamReader.readName(stream);
		String name = StreamReader.readName(stream);
		int type = stream.read();
		BinaryImportDesc desc = switch (type) {
		case TYPE_FUNC -> new BinaryFunctionImport(StreamReader.readUint32Var(stream));
		case TYPE_TABLE -> new BinaryTableImport(moduleParser.parseTableType(stream));
		case TYPE_MEM -> new BinaryMemoryImport(moduleParser.parseMemoryType(stream));
		case TYPE_GLOBAL -> new BinaryGlobalImport(moduleParser.parseGlobalType(stream));
		case -1 -> throw new EOFException();
		default -> throw new IOException("Import type not implemented: 0x%02d".formatted(type));
		};
		return new BinaryImport(module, name, desc);
	}
}
