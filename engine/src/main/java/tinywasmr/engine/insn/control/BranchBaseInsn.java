package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.frame.BlockFrame;
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
		FunctionFrame funcFrame = vm.peekFunctionFrame();
		Frame targetFrame = vm.getFrameStack().get(vm.getFrameStack().size() - nestIndex() - 1);

		if (targetFrame instanceof BlockFrame blockFrame && blockFrame.getBlock() instanceof LoopInsn loopInsn) {
			// br act like continue statement
			for (int i = 0; i <= nestIndex(); i++) {
				Frame frame = vm.popFrame();
				if (frame == targetFrame) break;
				if (frame == funcFrame) break; // can't pop past function frame
			}

			loopInsn.execute(vm);
		} else {
			// br exit the block
			List<ValueType> resultTypes = targetFrame.getBranchResultTypes().blockResults();
			Value[] results = new Value[resultTypes.size()];

			for (int i = results.length - 1; i >= 0; i--) {
				Value val = vm.peekFrame().popOprand();
				if (vm.hasRuntimeValidation() && !val.type().equals(resultTypes.get(i)))
					throw new ValidationException("Type mismatch: %s (stack) != %s (block)"
						.formatted(val.type(), resultTypes.get(i)));
				results[i] = val;
			}

			for (int i = 0; i <= nestIndex(); i++) {
				Frame frame = vm.popFrame();
				if (frame == targetFrame) break;
				if (frame == funcFrame) break; // can't pop past function frame
			}

			for (Value val : results) vm.peekFrame().pushOperand(val);
		}
	}
}
