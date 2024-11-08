package tinywasmr.parser.binary.imprt;

import tinywasmr.engine.module.imprt.ImportDescription;
import tinywasmr.engine.type.GlobalType;
import tinywasmr.parser.binary.BinaryModuleLayout;

public record BinaryGlobalImport(GlobalType type) implements BinaryImportDesc {
	@Override
	public ImportDescription build(BinaryModuleLayout view) {
		throw new RuntimeException("Not implemeted");
	}
}
