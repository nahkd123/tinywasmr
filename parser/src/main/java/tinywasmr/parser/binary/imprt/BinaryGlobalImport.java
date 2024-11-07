package tinywasmr.parser.binary.imprt;

import tinywasmr.engine.module.imprt.ImportDescription;
import tinywasmr.engine.type.GlobalType;
import tinywasmr.parser.binary.BinaryIndicesView;

record BinaryGlobalImport(GlobalType type) implements BinaryImportDesc {
	@Override
	public ImportDescription build(BinaryIndicesView view) {
		throw new RuntimeException("Not implemeted");
	}
}
