package tinywasmr.engine.insn.variable;

import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.global.GlobalDecl;

public record GlobalInsn(GlobalInsnType type, GlobalDecl global) implements Instruction {
	@Override
	public void execute(Machine vm) {
		type.execute(vm, global);
	}
}
