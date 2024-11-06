package tinywasmr.engine.insn.control;

import tinywasmr.engine.exec.vm.Machine;

public record BranchIfInsn(int nestIndex) implements BranchBaseInsn {
	@Override
	public void execute(Machine vm) {
		if (vm.peekFrame().popOprand().condition()) BranchBaseInsn.super.execute(vm);
	}
}
