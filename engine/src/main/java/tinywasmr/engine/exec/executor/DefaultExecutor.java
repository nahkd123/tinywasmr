package tinywasmr.engine.exec.executor;

import java.util.List;

import tinywasmr.engine.exec.StepResult;
import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.trap.ExternalTrap;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.type.value.ValueType;

/**
 * <p>
 * The default implementation of {@link Executor}.
 * </p>
 */
public class DefaultExecutor implements Executor {
	@Override
	public StepResult step(Machine vm) {
		if (vm.getTrap() != null) return StepResult.TRAP;
		if (vm.peekFrame() == vm.getExternalFrame()) {
			vm.setTrap(new ExternalTrap(new IllegalStateException("Can't step in external frame")));
			return StepResult.TRAP;
		}

		while (vm.peekFrame().getInsnIndex() >= vm.peekFrame().getExecutingInsns().size()) {
			List<ValueType> resultTypes = vm.peekFrame().getBranchResultTypes().blockResults();
			Value[] results = new Value[resultTypes.size()];

			for (int i = results.length - 1; i >= 0; i--) {
				Value val = vm.peekFrame().popOprand();

				if (vm.hasRuntimeValidation() && !val.type().equals(resultTypes.get(i))) {
					vm.setTrap(new ExternalTrap(new ValidationException("Type mismatch: %s (stack) != %s (bresult)"
						.formatted(val.type(), resultTypes.get(i)))));
					return StepResult.TRAP;
				}

				results[i] = val;
			}

			vm.popFrame();
			for (Value val : results) vm.peekFrame().pushOperand(val);
			if (vm.peekFrame() == vm.getExternalFrame()) return StepResult.NORMAL;
		}

		Frame frame = vm.peekFrame();

		try {
			frame.getExecutingInsns().get(frame.getInsnIndex()).execute(vm);
			return StepResult.NORMAL;
		} catch (Throwable e) {
			vm.setTrap(new ExternalTrap(e));
			return StepResult.TRAP;
		} finally {
			frame.incInsnIndex();
		}
	}
}
