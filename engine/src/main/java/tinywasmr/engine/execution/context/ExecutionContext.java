package tinywasmr.engine.execution.context;

import java.util.List;

import tinywasmr.engine.execution.ModuleInstance;
import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.execution.stack.MachineStack;
import tinywasmr.engine.execution.value.ValueHolder;

public interface ExecutionContext {
	public ModuleInstance getInstance();

	public MachineStack getStack();

	public List<ValueHolder> getLocals();

	public void triggerReturn();

	public boolean isReturned();

	public void setBranchOutDepth(int depth);

	public int getBranchOutDepth();

	default ValueHolder getLocalOrTrap(int pos) {
		var locals = getLocals();
		if (pos < 0 || pos >= locals.size()) throw new TrapException("Invalid local index " + pos);
		return locals.get(pos);
	}
}
