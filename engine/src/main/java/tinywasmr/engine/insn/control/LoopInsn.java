package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.frame.LoopFrame;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.BlockType;

public record LoopInsn(BlockType blockType, List<Instruction> instructions) implements Instruction {
	@Override
	public void execute(Machine vm) {
		vm.pushFrame(new LoopFrame(this));
	}
}
