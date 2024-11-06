package tinywasmr.engine.insn.variable;

import tinywasmr.engine.exec.vm.Machine;

public enum LocalInsnType {
	GET {
		@Override
		public void executeAsInsn(Machine machine, int index) {
			machine.peekFrame().pushOperand(machine.peekFunctionFrame().getLocals()[index]);
		}
	},
	SET {
		@Override
		public void executeAsInsn(Machine machine, int index) {
			machine.peekFunctionFrame().getLocals()[index] = machine.peekFrame().popOprand();
		}
	},
	TEE {
		@Override
		public void executeAsInsn(Machine machine, int index) {
			machine.peekFunctionFrame().getLocals()[index] = machine.peekFrame().peekOperand();
		}
	};

	public abstract void executeAsInsn(Machine machine, int index);
}
