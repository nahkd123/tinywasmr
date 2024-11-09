package tinywasmr.engine.module.imprt;

import tinywasmr.engine.type.GlobalType;

public record GlobalImportDescription(GlobalType type) implements ImportDescription {
}
