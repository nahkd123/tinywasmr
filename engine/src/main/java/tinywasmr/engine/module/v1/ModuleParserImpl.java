package tinywasmr.engine.module.v1;

import java.io.IOException;

import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.module.ModuleParser;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.section.Section;
import tinywasmr.engine.module.type.Type;
import tinywasmr.engine.module.v1.function.FunctionImpl;
import tinywasmr.engine.module.v1.importing.FunctionImportImpl;
import tinywasmr.engine.module.v1.section.CodeSectionImpl;
import tinywasmr.engine.module.v1.section.CustomSectionImpl;
import tinywasmr.engine.module.v1.section.FunctionsSectionImpl;
import tinywasmr.engine.module.v1.section.ImportsSectionImpl;
import tinywasmr.engine.module.v1.section.MemorySectionImpl;
import tinywasmr.engine.module.v1.section.TypesSectionImpl;
import tinywasmr.engine.module.v1.section.UnknownSectionImpl;
import tinywasmr.engine.module.v1.type.FunctionTypeImpl;
import tinywasmr.engine.module.v1.type.PrimitiveTypeImpl;

public class ModuleParserImpl implements ModuleParser {
	@Override
	public int getFormatVersion() { return 1; }

	@Override
	public WasmModule parse(LEDataInput in) throws IOException {
		var module = new WasmModuleImpl();
		Section section;
		while ((section = parseSection(in)) != null) module.getModifiableSectionsList().add(section);
		linkModule(module);
		return module;
	}

	public Section parseSection(LEDataInput in) throws IOException {
		var id = in.readByte();
		if (id == -1) return null;

		var size = (int) in.readLEB128();
		return parseSection1(id, size, in);
	}

	// https://webassembly.github.io/spec/core/binary/modules.html#sections
	private static final int SECTION_CUSTOM = 0x00;
	private static final int SECTION_TYPES = 0x01;
	private static final int SECTION_IMPORTS = 0x02;
	private static final int SECTION_FUNCTIONS = 0x03;
	// TODO table
	private static final int SECTION_MEMORY = 0x05;
	// TODO
	private static final int SECTION_CODE = 0x0A;

	public Section parseSection1(int id, int size, LEDataInput in) throws IOException {
		return switch (id) {
		case SECTION_CUSTOM -> new CustomSectionImpl(in, size);
		case SECTION_TYPES -> new TypesSectionImpl(in, this);
		case SECTION_IMPORTS -> new ImportsSectionImpl(in);
		case SECTION_FUNCTIONS -> new FunctionsSectionImpl(in);
		case SECTION_MEMORY -> new MemorySectionImpl(in);
		case SECTION_CODE -> new CodeSectionImpl(in, this);
		default -> new UnknownSectionImpl(id, in.readBytes(size));
		};
	}

	// https://webassembly.github.io/spec/core/binary/types.html#types
	private static final int TYPE_I32 = 0x7F;
	private static final int TYPE_I64 = 0x7E;
	private static final int TYPE_F32 = 0x7D;
	private static final int TYPE_F64 = 0x7C;
	private static final int TYPE_V128 = 0x7B;
	private static final int TYPE_FUNCTION = 0x60;

	public Type parseType(LEDataInput in) throws IOException {
		int id = in.readByte();
		return switch (id) {
		case TYPE_I32 -> PrimitiveTypeImpl.I32;
		case TYPE_I64 -> PrimitiveTypeImpl.I64;
		case TYPE_F32 -> PrimitiveTypeImpl.F32;
		case TYPE_F64 -> PrimitiveTypeImpl.F64;
		case TYPE_V128 -> PrimitiveTypeImpl.V128;
		case TYPE_FUNCTION -> new FunctionTypeImpl(in.readVector(s -> parseType(s)), in.readVector(s -> parseType(s)));
		default -> throw new IOException("Unknown type ID or unimplemented type ID: " + id);
		};
	}

	private void linkModule(WasmModuleImpl module) {
		var importsOpt = module.getImportsSection();
		if (importsOpt.isPresent()) {
			for (var e : importsOpt.get().getImports()) {
				if (e instanceof FunctionImportImpl func) func.link(module);
				// TODO
			}
		}

		var functionsOpt = module.getFunctionsSection();
		if (functionsOpt.isPresent()) {
			for (var e : functionsOpt.get().getFunctions())
				if (e instanceof FunctionImpl func) func.link(module);
		}
	}
}
