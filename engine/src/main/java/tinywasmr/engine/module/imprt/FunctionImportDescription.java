package tinywasmr.engine.module.imprt;

import tinywasmr.engine.type.FunctionType;

public record FunctionImportDescription(FunctionType type) implements ImportDescription {
}
