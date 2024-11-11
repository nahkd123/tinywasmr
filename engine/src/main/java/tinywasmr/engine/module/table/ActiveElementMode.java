package tinywasmr.engine.module.table;

import java.util.List;

import tinywasmr.engine.insn.Instruction;

public record ActiveElementMode(TableDecl table, List<Instruction> offsetExpr) implements ElementMode {
}
