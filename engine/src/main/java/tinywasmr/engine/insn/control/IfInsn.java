package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.frame.IfFrame;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.BlockType;

public record IfInsn(BlockType blockType, List<Instruction> truePath, List<Instruction> falsePath) implements Instruction {
	@Override
	public void execute(Machine vm) {
		boolean trueBranch = vm.peekFrame().popOprand().condition();
		vm.pushFrame(new IfFrame(this, trueBranch));
	}
}
