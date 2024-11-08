package tinywasmr.parser.binary;

import java.util.List;

import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.table.TableDecl;
import tinywasmr.engine.type.FunctionType;

/**
 * <p>
 * A layout view of binary module. This will be used to resolve indices into
 * types and declarations.
 * </p>
 */
public record BinaryModuleLayout(List<FunctionType> types, List<TableDecl> tables, List<MemoryDecl> memories, List<FunctionDecl> functions) {
}
