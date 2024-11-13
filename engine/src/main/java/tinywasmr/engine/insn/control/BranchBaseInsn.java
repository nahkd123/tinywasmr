package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.frame.BlockFrame;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.IfFrame;
import tinywasmr.engine.exec.frame.LoopFrame;
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
	int nestIndex();

	static int countBlockDepth(Machine vm) {
		for (int i = vm.getFrameStack().size() - 1; i >= 0; i--) {
			if (!(vm.getFrameStack().get(i) instanceof BlockFrame ||
				vm.getFrameStack().get(i) instanceof IfFrame ||
				vm.getFrameStack().get(i) instanceof LoopFrame)) {
				return vm.getFrameStack().size() - i;
			}
		}

		throw new IllegalStateException("Unable to count depth; no external frame?");
	}

	static void execute(Machine vm, int index) {
		int depth = countBlockDepth(vm);
		if (index >= depth) throw new IllegalArgumentException("Illegal br label: %d".formatted(index));

		Frame targetFrame = vm.getFrameStack().get(vm.getFrameStack().size() - index - 1);
		targetFrame.branchThis();

		if (targetFrame.isFrameFinished()) {
			List<ValueType> resultTypes = targetFrame.getBranchResultTypes().blockResults();
			Value[] results = new Value[resultTypes.size()];

			for (int i = results.length - 1; i >= 0; i--) {
				Value val = vm.peekFrame().popOprand();
				// TODO validation
				results[i] = val;
			}

			for (Value val : results) targetFrame.pushOperand(val);
		}

		while (vm.peekFrame() != targetFrame) vm.popFrame();
	}

	/**
	 * <p>
	 * By default, this will perform actions like unconditional branch. You can
	 * extends this to branch based on specific condition.
	 * </p>
	 */
	@Override
	default void execute(Machine vm) {
		execute(vm, nestIndex());
	}
}
