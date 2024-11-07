package tinywasmr.engine.insn.parametric;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;

public enum ParametricInsn implements Instruction {
	DROP {
		@Override
		public void execute(Machine vm) {
			vm.peekFrame().popOprand();
		}
	},
	SELECT_AUTO {
		@Override
		public void execute(Machine vm) {
			Value test = vm.peekFrame().popOprand();
			Value top = vm.peekFrame().popOprand();
			Value bottom = vm.peekFrame().popOprand();

			if (vm.hasRuntimeValidation() && !bottom.type().equals(top.type()))
				throw new ValidationException("Type mismatch: %s (bottom) != %s (top)"
					.formatted(bottom.type(), top.type()));

			vm.peekFrame().pushOperand(test.condition() ? bottom : top);
		}
	};

	@Override
	public abstract void execute(Machine vm);
}
