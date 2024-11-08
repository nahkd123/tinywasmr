package tinywasmr.engine.module.imprt;

import tinywasmr.engine.type.MemoryType;

public record MemoryImportDescription(MemoryType type) implements ImportDescription {
}
