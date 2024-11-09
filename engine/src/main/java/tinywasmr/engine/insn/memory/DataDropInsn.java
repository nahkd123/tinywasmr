package tinywasmr.engine.insn.memory;

import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.memory.DataSegment;

public record DataDropInsn(DataSegment segment) implements Instruction {
	@Override
	public void execute(Machine vm) {
		// Do nothing for the time being
	}
}
