package tinywasmr.engine.insn.variable;

import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;

public record LocalInsn(LocalInsnType type, int index) implements Instruction {
	@Override
	public void execute(Machine vm) {
		type.executeAsInsn(vm, index);
	}
}
