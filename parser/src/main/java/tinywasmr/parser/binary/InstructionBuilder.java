package tinywasmr.parser.binary;

import tinywasmr.engine.insn.Instruction;

@FunctionalInterface
interface InstructionBuilder {
	Instruction build(BinaryIndicesView view);
}