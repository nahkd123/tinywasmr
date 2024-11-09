package tinywasmr.engine.module.export;

import tinywasmr.engine.module.global.GlobalDecl;

public record GlobalExportDescription(GlobalDecl global) implements ExportDescription {
}
