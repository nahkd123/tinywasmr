package tinywasmr.parser.binary.imprt;

import tinywasmr.engine.module.imprt.FunctionImportDescription;
import tinywasmr.engine.module.imprt.ImportDescription;
import tinywasmr.parser.binary.BinaryModuleLayout;

public record BinaryFunctionImport(int typeIndex) implements BinaryImportDesc {
	@Override
	public ImportDescription build(BinaryModuleLayout view) {
		return new FunctionImportDescription(view.types().get(typeIndex));
	}
}
