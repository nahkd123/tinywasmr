package tinywasmr.parser.binary.imprt;

import tinywasmr.engine.module.imprt.ImportDescription;
import tinywasmr.engine.type.TableType;
import tinywasmr.parser.binary.BinaryIndicesView;

record BinaryTableImport(TableType type) implements BinaryImportDesc {
	@Override
	public ImportDescription build(BinaryIndicesView view) {
		throw new RuntimeException("Not implemeted");
	}
}
