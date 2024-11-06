package tinywasmr.engine.exec.executor;

import java.util.List;

import tinywasmr.engine.exec.StepResult;
import tinywasmr.engine.exec.frame.BlockFrame;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.trap.ExternalTrap;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.insn.control.LoopInsn;

/**
 * <p>
 * The default implementation of {@link Executor}.
 * </p>
 */
public class DefaultExecutor implements Executor {
	@Override
	public StepResult step(Machine vm) {
		if (vm.getTrap() != null) return StepResult.TRAP;
		Frame frame = vm.peekFrame();

		if (frame == vm.getExternalFrame()) {
			vm.setTrap(new ExternalTrap(new IllegalStateException("Can't step in external frame")));
			return StepResult.TRAP;
		}

		List<Instruction> insns = frame.getExecutingInsns();

		while (frame.getInsnIndex() >= insns.size()) {
			vm.popFrame();

			if (frame instanceof BlockFrame blockFrame && blockFrame.getBlock() instanceof LoopInsn loop) {
				loop.execute(vm);
				return StepResult.NORMAL;
			} else {
				for (Value val : frame.getOperandStack()) vm.peekFrame().pushOperand(val);
				frame = vm.peekFrame();
			}

			if (frame == vm.getExternalFrame()) return StepResult.NORMAL;
		}

		try {
			insns.get(frame.getInsnIndex()).execute(vm);
			return StepResult.NORMAL;
		} catch (Throwable e) {
			vm.setTrap(new ExternalTrap(e));
			return StepResult.TRAP;
		} finally {
			frame.incInsnIndex();
		}
	}
}
