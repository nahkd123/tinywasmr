package tinywasmr.engine.insn.parametric;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.value.ValueType;

public record SelectExplictInsn(ValueType type) implements Instruction {
	@Override
	public void execute(Machine vm) {
		Value test = vm.peekFrame().popOprand();
		Value top = vm.peekFrame().popOprand();
		Value bottom = vm.peekFrame().popOprand();

		if (vm.hasRuntimeValidation()) {
			if (!top.type().equals(type)) throw new ValidationException("Type mismatch: %s (top) != %s (declared)"
				.formatted(top.type(), type));
			if (!bottom.type().equals(type)) throw new ValidationException("Type mismatch: %s (bottom) != %s (declared)"
				.formatted(bottom.type(), type));
		}

		vm.peekFrame().pushOperand(test.condition() ? bottom : top);
	}
}
