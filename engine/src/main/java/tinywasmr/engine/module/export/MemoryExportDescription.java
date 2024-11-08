package tinywasmr.engine.module.export;

import tinywasmr.engine.module.memory.MemoryDecl;

public record MemoryExportDescription(MemoryDecl memory) implements ExportDescription {
}
