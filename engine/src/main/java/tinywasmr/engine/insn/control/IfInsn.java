package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.frame.BlockFrame;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.BlockType;

public record IfInsn(BlockType blockType, List<Instruction> primary, List<Instruction> secondary) implements InstructionWithBlock {
	@Override
	public void execute(Machine vm) {
		boolean primary = vm.peekFrame().popOprand().condition();
		vm.pushFrame(new BlockFrame(this, primary));
	}
}
