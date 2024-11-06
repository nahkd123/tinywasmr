package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.value.ValueType;

public interface BranchBaseInsn extends Instruction {
	/**
	 * <p>
	 * Index of the frame in stack, starting from current frame (not bottom of the
	 * stack). Obviously it is that weird in WebAssembly.
	 * </p>
	 */
	public int nestIndex();

	/**
	 * <p>
	 * By default, this will perform actions like unconditional branch. You can
	 * extends this to branch based on specific condition.
	 * </p>
	 */
	@Override
	default void execute(Machine vm) {
		List<ValueType> resultTypes = vm.peekFrame().getBranchResultTypes().blockResults();
		Value[] results = new Value[resultTypes.size()];

		if (vm.peekFrame().getOperandStack().size() < results.length)
			throw new IllegalStateException("Operand stack must have at least %d values".formatted(results.length));

		for (int i = results.length - 1; i >= 0; i--) {
			Value val = vm.peekFrame().popOprand();

			if (vm.hasRuntimeValidation() && !val.type().equals(resultTypes.get(i)))
				throw new ValidationException("Type mismatch: %s (stack) != %s (declared)"
					.formatted(val.type(), resultTypes.get(i)));

			results[i] = val;
		}

		for (int i = 0; i <= nestIndex(); i++) {
			Frame poppedFrame = vm.popFrame();
			if (poppedFrame instanceof FunctionFrame) throw new IllegalStateException("Cannot pop past function frame");
		}

		for (Value val : results) vm.peekFrame().pushOperand(val);
	}
}
