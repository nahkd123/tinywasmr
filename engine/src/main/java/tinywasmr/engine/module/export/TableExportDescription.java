package tinywasmr.engine.module.export;

import tinywasmr.engine.module.table.TableDecl;

public record TableExportDescription(TableDecl table) implements ExportDescription {
}
