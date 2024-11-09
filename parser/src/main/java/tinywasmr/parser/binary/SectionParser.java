package tinywasmr.parser.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.MemoryType;
import tinywasmr.engine.type.TableType;
import tinywasmr.engine.type.value.ValueType;
import tinywasmr.parser.binary.imprt.BinaryImport;

public class SectionParser {
	public static SectionHeader parseSectionHeader(InputStream stream) throws IOException {
		int id = stream.read();
		if (id == -1) throw new EOFException();
		int size = StreamReader.readUint32Var(stream);
		return new SectionHeader(id, size);
	}

	public static CustomSection parseCustomSection(int size, InputStream stream, boolean ignore) throws IOException {
		if (ignore) {
			if (size != 0) {
				stream.skipNBytes(size);
				return null;
			}

			while (stream.available() > 0) stream.skip(4096);
			return null;
		}

		ByteCounterInputStream counter = new ByteCounterInputStream(stream);
		int nameLen = StreamReader.readUint32Var(counter);
		byte[] nameBs = stream.readNBytes(nameLen);
		if (nameBs.length != nameLen) throw new EOFException();
		String name = new String(nameBs, StandardCharsets.UTF_8);

		byte[] data;
		if (size == 0) data = stream.readAllBytes();
		else data = stream.readNBytes(size - counter.getCounter() - nameBs.length); // TODO: validation here?
		return new CustomSection(name, data);
	}

	public static List<FunctionType> parseTypeSection(int size, InputStream stream) throws IOException {
		int count = StreamReader.readUint32Var(stream);
		List<FunctionType> types = new ArrayList<>();
		for (int i = 0; i < count; i++) types.add(StreamReader.parseFunctionType(stream));
		return types;
	}

	public static int[] parseFunctionSection(int size, InputStream stream) throws IOException {
		int count = StreamReader.readUint32Var(stream);
		int[] functionTypeRefs = new int[count];
		for (int i = 0; i < count; i++) functionTypeRefs[i] = StreamReader.readUint32Var(stream);
		return functionTypeRefs;
	}

	public static TableType[] parseTableSection(int size, InputStream stream) throws IOException {
		int count = StreamReader.readUint32Var(stream);
		TableType[] tables = new TableType[count];
		for (int i = 0; i < count; i++) tables[i] = StreamReader.parseTableType(stream);
		return tables;
	}

	public static MemoryType[] parseMemorySection(int size, InputStream stream) throws IOException {
		int count = StreamReader.readUint32Var(stream);
		MemoryType[] memories = new MemoryType[count];
		for (int i = 0; i < count; i++) memories[i] = StreamReader.parseMemoryType(stream);
		return memories;
	}

	public static BinaryGlobal[] parseGlobalSection(int size, InputStream stream) throws IOException {
		int count = StreamReader.readUint32Var(stream);
		BinaryGlobal[] memories = new BinaryGlobal[count];
		for (int i = 0; i < count; i++) memories[i] = BinaryGlobal.parse(stream);
		return memories;
	}

	public static BinaryImport[] parseImportSection(int size, InputStream stream) throws IOException {
		int count = StreamReader.readUint32Var(stream);
		BinaryImport[] imports = new BinaryImport[count];
		for (int i = 0; i < count; i++) imports[i] = BinaryImport.parse(stream);
		return imports;
	}

	public static BinaryExport[] parseExportSection(int size, InputStream stream) throws IOException {
		int count = StreamReader.readUint32Var(stream);
		BinaryExport[] exports = new BinaryExport[count];
		for (int i = 0; i < count; i++) exports[i] = BinaryExport.parse(stream);
		return exports;
	}

	public static BinaryFunctionBody[] parseCodeSection(int size, InputStream stream) throws IOException {
		int count = StreamReader.readUint32Var(stream);
		BinaryFunctionBody[] functions = new BinaryFunctionBody[count];

		for (int i = 0; i < count; i++) {
			StreamReader.readUint32Var(stream); // function size in bytes, kept for side effect
			BinaryFunctionBody body = new BinaryFunctionBody(new ArrayList<>(), new ArrayList<>());
			int localsDeclCount = StreamReader.readUint32Var(stream);

			for (int j = 0; j < localsDeclCount; j++) {
				int multiples = StreamReader.readUint32Var(stream);
				ValueType type = StreamReader.parseValueType(stream);
				for (int k = 0; k < multiples; k++) body.locals().add(type);
			}

			while (true) {
				BinaryInstructionBuilder insn = CodeParser.parseInsn(stream);
				if (insn != null) body.body().add(insn);
				else break;
			}

			functions[i] = body;
		}

		return functions;
	}

	public static BinaryDataSegment[] parseDataSection(int size, InputStream stream) throws IOException {
		int count = StreamReader.readUint32Var(stream);
		BinaryDataSegment[] data = new BinaryDataSegment[count];
		for (int i = 0; i < count; i++) data[i] = BinaryDataSegment.parse(stream);
		return data;
	}

	public static int parseDataCountSection(int size, InputStream stream) throws IOException {
		return StreamReader.readUint32Var(stream);
	}
}
