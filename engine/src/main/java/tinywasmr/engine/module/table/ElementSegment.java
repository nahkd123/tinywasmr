package tinywasmr.engine.module.table;

import java.util.List;

import tinywasmr.engine.insn.control.BlockInsn;
import tinywasmr.engine.type.value.RefType;

public record ElementSegment(RefType type, List<BlockInsn> init, ElementMode mode) {
}
