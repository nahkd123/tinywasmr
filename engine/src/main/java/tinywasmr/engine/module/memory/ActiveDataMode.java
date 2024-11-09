package tinywasmr.engine.module.memory;

import java.util.List;

import tinywasmr.engine.insn.Instruction;

public record ActiveDataMode(MemoryDecl memory, List<Instruction> offsetExpr) implements DataMode {
}
