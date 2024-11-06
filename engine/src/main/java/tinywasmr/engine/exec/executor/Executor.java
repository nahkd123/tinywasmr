package tinywasmr.engine.exec.executor;

import java.util.List;

import tinywasmr.engine.exec.StepResult;
import tinywasmr.engine.exec.TrapException;
import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.trap.ExternalTrap;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.DefaultMachine;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.type.value.ValueType;

/**
 * <p>
 * Controls the flow of the execution. For example, you can limit the number of
 * instructions per second by adding sleep to {@link #step(Machine)}.
 * </p>
 */
public interface Executor {
	/**
	 * <p>
	 * Step by 1 instruction.
	 * </p>
	 *
	 * @param vm The virtual machine to step by 1 instruction.
	 * @return The result from stepping by 1 instruction.
	 */
	StepResult step(Machine vm);

	/**
	 * <p>
	 * Execute the function, using existing virtual machine.
	 * </p>
	 * 
	 * @param vm         The virtual machine to store the states. This is usually a
	 *                   new and fresh virtual machine, but you can pass existing
	 *                   virtual machine to execute function while the main code is
	 *                   still running.
	 * @param function   The function to execute.
	 * @param params     The input parameters to execute the function.
	 * @param clearStack Whether to clear the stack after execution. Usually this
	 *                   value should be {@code true} to ensure that the code won't
	 *                   crash, but during trace session, sometime you want to keep
	 *                   the results around.
	 * @return The results from executing the function. The length of this array is
	 *         equals to the number of outputs declared in the type of provided
	 *         function.
	 * @throws TrapException if trap is triggered during execution.
	 */
	default Value[] execute(Machine vm, Function function, Value[] params, boolean clearStack) throws TrapException {
		FunctionFrame frame = FunctionFrame.createCall(function, params);
		Frame marker = vm.peekFrame();
		vm.pushFrame(frame);

		while (vm.peekFrame() != marker) {
			StepResult stepResult = step(vm);

			if (stepResult == StepResult.TRAP) {
				if (vm.getTrap() instanceof ExternalTrap extern) throw new TrapException(extern.throwable());
				throw new TrapException();
			}
		}

		List<ValueType> resultTypes = function.declaration().type().outputs().types();
		Value[] results = new Value[resultTypes.size()];

		if (clearStack) {
			for (int i = results.length - 1; i >= 0; i--) {
				Value val = marker.popOprand();

				if (vm.hasRuntimeValidation() && !val.type().equals(resultTypes.get(i)))
					throw new TrapException(new ValidationException("Type mismatch: %s (stack) != %s (declared)"
						.formatted(val.type(), resultTypes.get(i))));

				results[i] = val;
			}
		} else {
			int stackSize = marker.getOperandStack().size();

			for (int i = 0; i < results.length; i++) {
				Value val = marker.getOperandStack().get(stackSize - results.length + i);

				if (vm.hasRuntimeValidation() && !val.type().equals(resultTypes.get(i)))
					throw new TrapException(new ValidationException("Type mismatch: %s (stack) != %s (declared)"
						.formatted(val.type(), resultTypes.get(i))));

				results[i] = val;
			}
		}

		return results;
	}

	default Value[] execute(Machine vm, Function function, Value[] params) throws TrapException {
		return execute(vm, function, params, true);
	}

	default Value[] execute(Function function, Value[] params) throws TrapException {
		return execute(new DefaultMachine(), function, params);
	}
}
