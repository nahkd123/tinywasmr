package tinywasmr.engine.insn.numeric;

import tinywasmr.engine.exec.trap.ExternalTrap;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.ValueType;

public enum BinaryOpInsn implements Instruction {
	I32_ADD(NumberType.I32) {
		@Override
		public Value apply(Value bottom, Value top) {
			return new NumberI32Value(bottom.i32() + top.i32());
		}
	};

	private ValueType bottomType;
	private ValueType topType;

	private BinaryOpInsn(ValueType bottomType, ValueType topType) {
		this.bottomType = bottomType;
		this.topType = topType;
	}

	private BinaryOpInsn(ValueType type) {
		this(type, type);
	}

	public abstract Value apply(Value bottom, Value top);

	@Override
	public void execute(Machine vm) {
		Value top = vm.peekFrame().popOprand();
		Value bottom = vm.peekFrame().popOprand();

		if (vm.hasRuntimeValidation()) {
			if (!top.type().equals(topType)) {
				// trap:
				vm.setTrap(new ExternalTrap(new IllegalArgumentException("Expected %s at top of the stack, found %s"
					.formatted(topType, top.type()))));
				return;
			}

			if (!bottom.type().equals(bottomType)) {
				// trap:
				vm.setTrap(new ExternalTrap(new IllegalArgumentException("Expected %s at bottom of the stack, found %s"
					.formatted(topType, bottom.type()))));
				return;
			}
		}

		vm.peekFrame().pushOperand(apply(bottom, top));
	}
}
