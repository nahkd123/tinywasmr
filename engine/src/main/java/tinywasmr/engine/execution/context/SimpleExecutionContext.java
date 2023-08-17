package tinywasmr.engine.execution.context;

import java.util.List;

import tinywasmr.engine.execution.ModuleInstance;
import tinywasmr.engine.execution.stack.MachineStack;
import tinywasmr.engine.execution.stack.StackBackedMachineStack;
import tinywasmr.engine.execution.value.LocalsBuilder;
import tinywasmr.engine.execution.value.ValueHolder;

public class SimpleExecutionContext implements ExecutionContext {
	private ModuleInstance instance;
	private MachineStack stack = new StackBackedMachineStack();
	private List<ValueHolder> locals;
	private boolean isReturned = false;
	private int branchOutDepth = 0;

	public SimpleExecutionContext(ModuleInstance instance, LocalsBuilder locals) {
		this.instance = instance;
		this.locals = locals.build();
	}

	@Override
	public ModuleInstance getInstance() { return instance; }

	@Override
	public MachineStack getStack() { return stack; }

	@Override
	public List<ValueHolder> getLocals() { return locals; }

	@Override
	public void triggerReturn() {
		isReturned = true;
	}

	@Override
	public boolean isReturned() { return isReturned; }

	@Override
	public int getBranchOutDepth() { return branchOutDepth; }

	@Override
	public void setBranchOutDepth(int branchOutDepth) { this.branchOutDepth = branchOutDepth; }
}
