package tinywasmr.parser.binary.imprt;

import tinywasmr.engine.module.imprt.ImportDescription;
import tinywasmr.engine.module.imprt.MemoryImportDescription;
import tinywasmr.engine.type.MemoryType;
import tinywasmr.parser.binary.BinaryModuleLayout;

public record BinaryMemoryImport(MemoryType type) implements BinaryImportDesc {
	@Override
	public ImportDescription build(BinaryModuleLayout view) {
		return new MemoryImportDescription(type);
	}
}
