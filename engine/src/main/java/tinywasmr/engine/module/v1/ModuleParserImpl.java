package tinywasmr.engine.module.v1;

import java.io.IOException;

import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.module.ModuleParser;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.section.Section;
import tinywasmr.engine.module.v1.section.CustomSectionImpl;
import tinywasmr.engine.module.v1.section.UnknownSectionImpl;

public class ModuleParserImpl implements ModuleParser {
	@Override
	public int getFormatVersion() { return 1; }

	@Override
	public WasmModule parse(LEDataInput in) throws IOException {
		var module = new WasmModuleImpl();
		Section section;
		while ((section = parseSection(in)) != null) module.getModifiableSectionsList().add(section);
		return module;
	}

	private Section parseSection(LEDataInput in) throws IOException {
		var id = in.readByte();
		if (id == -1) return null;

		var size = (int) in.readLEB128();
		return parseSection1(id, size, in);
	}

	private static final int SECTION_CUSTOM = 0x00;

	private Section parseSection1(int id, int size, LEDataInput in) throws IOException {
		return switch (id) {
		case SECTION_CUSTOM -> new CustomSectionImpl(in);
		default -> new UnknownSectionImpl(id, in.readBytes(size));
		};
	}
}
