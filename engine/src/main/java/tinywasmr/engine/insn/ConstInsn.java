package tinywasmr.engine.insn;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;

/**
 * <p>
 * Push a constant to operand stack.
 * </p>
 */
public record ConstInsn(Value value) implements Instruction {
	@Override
	public void execute(Machine vm) {
		vm.peekFrame().pushOperand(value);
	}
}
