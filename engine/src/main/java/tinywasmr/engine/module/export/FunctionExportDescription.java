package tinywasmr.engine.module.export;

import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.ModuleFunctionDecl;

/**
 * <p>
 * A description for function export. WebAssembly may choose to re-export the
 * imported function, which is why the function type in here is not limited to
 * just {@link ModuleFunctionDecl}.
 * </p>
 */
public record FunctionExportDescription(FunctionDecl function) implements ExportDescription {
}
