package tinywasmr.parser.binary.imprt;

import tinywasmr.engine.module.imprt.ImportDescription;
import tinywasmr.parser.binary.BinaryModuleLayout;

public interface BinaryImportDesc {
	ImportDescription build(BinaryModuleLayout view);
}
