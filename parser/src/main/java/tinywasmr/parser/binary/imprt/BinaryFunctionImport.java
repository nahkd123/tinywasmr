package tinywasmr.parser.binary.imprt;

import tinywasmr.engine.module.imprt.FunctionImportDescription;
import tinywasmr.engine.module.imprt.ImportDescription;
import tinywasmr.parser.binary.BinaryIndicesView;

record BinaryFunctionImport(int typeIndex) implements BinaryImportDesc {
	@Override
	public ImportDescription build(BinaryIndicesView view) {
		return new FunctionImportDescription(view.types().get(typeIndex));
	}
}
