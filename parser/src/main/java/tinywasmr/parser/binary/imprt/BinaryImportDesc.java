package tinywasmr.parser.binary.imprt;

import tinywasmr.engine.module.imprt.ImportDescription;
import tinywasmr.parser.binary.BinaryIndicesView;

public interface BinaryImportDesc {
	ImportDescription build(BinaryIndicesView view);
}
