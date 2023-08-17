package tinywasmr.engine.module.v1.section;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.module.importing.Import;
import tinywasmr.engine.module.section.ImportsSection;
import tinywasmr.engine.module.v1.importing.FunctionImportImpl;

public class ImportsSectionImpl implements ImportsSection {
	private static final int DESC_FUNCTION = 0x00;
	// TODO

	private List<Import> imports;

	public ImportsSectionImpl(List<Import> imports) {
		this.imports = imports;
	}

	public ImportsSectionImpl(LEDataInput in) throws IOException {
		long count = in.readLEB128Unsigned();
		imports = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			var module = in.readUTF8();
			var name = in.readUTF8();
			var descId = in.readByte();
			int idx;

			switch (descId) {
			case DESC_FUNCTION:
				idx = (int) in.readLEB128Unsigned();
				imports.add(new FunctionImportImpl(module, name, idx));
				break;
			default:
				throw new IOException("Unknown import description ID: " + descId);
			}
		}
	}

	@Override
	public List<Import> getImports() { return Collections.unmodifiableList(imports); }
}
