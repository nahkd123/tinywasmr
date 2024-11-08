package tinywasmr.parser.binary.imprt;

import tinywasmr.engine.module.imprt.TableImportDescription;
import tinywasmr.engine.type.TableType;
import tinywasmr.parser.binary.BinaryModuleLayout;

public record BinaryTableImport(TableType type) implements BinaryImportDesc {
	@Override
	public TableImportDescription build(BinaryModuleLayout view) {
		return new TableImportDescription(type);
	}
}
