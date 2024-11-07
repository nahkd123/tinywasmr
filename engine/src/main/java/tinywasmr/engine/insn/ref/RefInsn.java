package tinywasmr.engine.insn.ref;

import tinywasmr.engine.exec.value.RefValue;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;

public enum RefInsn implements Instruction {
	NULL_EXTERN {
		@Override
		public void execute(Machine vm) {
			vm.peekFrame().pushOperand(RefValue.NULL_EXTERN);
		}
	},
	NULL_FUNC {
		@Override
		public void execute(Machine vm) {
			vm.peekFrame().pushOperand(RefValue.NULL_FUNC);
		}
	},
	IS_NULL {
		@Override
		public void execute(Machine vm) {
			Value val = vm.peekFrame().popOprand();
			if (vm.hasRuntimeValidation() && !(val instanceof RefValue))
				throw new IllegalArgumentException("Expected funcref or externref, but %s found".formatted(val.type()));
			vm.peekFrame().pushOperand(val.condition() ? Value.FALSE : Value.TRUE);
		}
	};

	@Override
	public abstract void execute(Machine vm);
}
