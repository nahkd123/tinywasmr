package tinywasmr.engine.module.table;

import java.util.List;

import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.value.RefType;

public record ElementSegment(RefType type, List<List<Instruction>> inits, ElementMode mode) {
}
