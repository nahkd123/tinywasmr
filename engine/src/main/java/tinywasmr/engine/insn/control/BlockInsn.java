package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.frame.BlockFrame;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.BlockType;

public record BlockInsn(BlockType blockType, List<Instruction> instructions) implements Instruction {
	@Override
	public void execute(Machine vm) {
		vm.pushFrame(new BlockFrame(this));
	}
}
