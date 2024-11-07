package tinywasmr.parser.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.value.ValueType;
import tinywasmr.parser.binary.imprt.BinaryImport;

class SectionParser {
	public static SectionHeader parseSectionHeader(BinaryModuleParser moduleParser, InputStream stream) throws IOException {
		moduleParser.getLogger().verbose("begin parsing section header");
		int id = stream.read();
		if (id == -1) throw new EOFException();
		int size = StreamReader.readUint32Var(stream);
		moduleParser.getLogger().verbose("section 0x%02x, size = %d (guessing = %s)", id, size, size == 0);
		moduleParser.getLogger().verbose("end parsing section header");
		return new SectionHeader(id, size);
	}

	public static CustomSection parseCustomSection(BinaryModuleParser moduleParser, int size, InputStream stream) throws IOException {
		moduleParser.getLogger().verbose("begin parsing custom section");

		if (moduleParser.isIgnoreCustomSections()) {
			moduleParser.getLogger().verbose("ignoring custom section");

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
		moduleParser.getLogger().verbose("custom section %s, content size = %d", name, data.length);
		moduleParser.getLogger().verbose("end parsing custom section");
		return new CustomSection(name, data);
	}

	public static List<FunctionType> parseTypeSection(BinaryModuleParser moduleParser, int size, InputStream stream) throws IOException {
		moduleParser.getLogger().verbose("begin parsing type section");
		int count = StreamReader.readUint32Var(stream);
		List<FunctionType> types = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			FunctionType functionType = moduleParser.parseFunctionType(stream);
			types.add(functionType);
		}

		moduleParser.getLogger().verbose("type section %d types", count);
		moduleParser.getLogger().verbose("end parsing type section");
		return types;
	}

	public static int[] parseFunctionSection(BinaryModuleParser moduleParser, int size, InputStream stream) throws IOException {
		moduleParser.getLogger().verbose("begin parsing function section");
		int count = StreamReader.readUint32Var(stream);
		int[] functionTypeRefs = new int[count];
		for (int i = 0; i < count; i++) functionTypeRefs[i] = StreamReader.readUint32Var(stream);
		moduleParser.getLogger().verbose("function section %d functions", count);
		moduleParser.getLogger().verbose("end parsing function section");
		return functionTypeRefs;
	}

	public static BinaryImport[] parseImportSection(BinaryModuleParser moduleParser, int size, InputStream stream) throws IOException {
		moduleParser.getLogger().verbose("begin parsing import section");
		int count = StreamReader.readUint32Var(stream);
		BinaryImport[] imports = new BinaryImport[count];
		moduleParser.getLogger().verbose("import section $d imports", count);

		for (int i = 0; i < count; i++) {
			imports[i] = BinaryImport.parse(moduleParser, stream);
			moduleParser.getLogger().verbose("  import #%d: %s::%s", i, imports[i].module(), imports[i].name());
		}

		moduleParser.getLogger().verbose("end parsing import section");
		return imports;
	}

	public static BinaryExport[] parseExportSection(BinaryModuleParser moduleParser, int size, InputStream stream) throws IOException {
		moduleParser.getLogger().verbose("begin parsing export section");
		int count = StreamReader.readUint32Var(stream);
		BinaryExport[] exports = new BinaryExport[count];
		moduleParser.getLogger().verbose("export section $d exports", count);

		for (int i = 0; i < count; i++) {
			exports[i] = BinaryExport.parse(stream);
			moduleParser.getLogger().verbose("  export #%d: %d -> %s", i, exports[i].index(), exports[i].name());
		}

		moduleParser.getLogger().verbose("end parsing export section");
		return exports;
	}

	public static BinaryFunctionBody[] parseCodeSection(BinaryModuleParser moduleParser, int size, InputStream stream) throws IOException {
		moduleParser.getLogger().verbose("begin parsing code section");
		int count = StreamReader.readUint32Var(stream);
		BinaryFunctionBody[] functions = new BinaryFunctionBody[count];

		for (int i = 0; i < count; i++) {
			int funcSize = StreamReader.readUint32Var(stream);
			moduleParser.getLogger().verbose("function #%d: size = %d (guess = %s)", i, funcSize, funcSize == 0);

			BinaryFunctionBody body = new BinaryFunctionBody(new ArrayList<>(), new ArrayList<>());
			int localsDeclCount = StreamReader.readUint32Var(stream);
			moduleParser.getLogger().verbose("  %d local declarations", localsDeclCount);

			for (int j = 0; j < localsDeclCount; j++) {
				int multiples = StreamReader.readUint32Var(stream);
				ValueType type = moduleParser.parseValueType(stream);
				for (int k = 0; k < multiples; k++) body.locals().add(type);
				moduleParser.getLogger().verbose("    %dx of %d", multiples, type);
			}

			while (true) {
				InstructionBuilder insn = CodeParser.parseInsn(moduleParser, stream);
				if (insn != null) body.body().add(insn);
				else break;
			}

			functions[i] = body;
		}

		moduleParser.getLogger().verbose("end parsing code section");
		return functions;
	}
}
