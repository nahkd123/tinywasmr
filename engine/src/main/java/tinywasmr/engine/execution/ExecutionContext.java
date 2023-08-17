package tinywasmr.engine.execution;

import java.util.List;

import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.execution.stack.MachineStack;
import tinywasmr.engine.execution.value.ValueHolder;

public interface ExecutionContext {
	public MachineStack getStack();

	public List<ValueHolder> getLocals();

	public void triggerReturn();

	default ValueHolder getLocalOrTrap(int pos) {
		var locals = getLocals();
		if (pos < 0 || pos >= locals.size()) throw new TrapException("Invalid local index " + pos);
		return locals.get(pos);
	}
}
