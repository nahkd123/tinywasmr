package tinywasmr.engine.module.imprt;

import tinywasmr.engine.type.TableType;

public record TableImportDescription(TableType type) implements ImportDescription {
}
