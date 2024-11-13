package tinywasmr.engine.insn.variable;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.module.global.GlobalDecl;

public enum GlobalInsnType {
	GET {
		@Override
		public void execute(Machine vm, GlobalDecl global) {
			Value val = vm.peekInstancedFrame().getInstance().global(global).get();
			vm.peekFrame().pushOperand(val);
		}
	},
	SET {
		@Override
		public void execute(Machine vm, GlobalDecl global) {
			Value val = vm.peekFrame().popOprand();
			vm.peekInstancedFrame().getInstance().global(global).set(val);
		}
	};

	public abstract void execute(Machine vm, GlobalDecl global);
}
