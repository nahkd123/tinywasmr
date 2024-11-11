package tinywasmr.engine.insn.control;

import tinywasmr.engine.exec.vm.Machine;

public record BranchTableInsn(int[] labels, int defaultLabel) implements BranchBaseInsn {
	@Override
	public int nestIndex() {
		return -1;
	}

	@Override
	public void execute(Machine vm) {
		int i = vm.peekFrame().popOprand().i32();
		BranchBaseInsn.execute(vm, i >= 0 && i < labels.length ? labels[i] : defaultLabel);
	}
}
