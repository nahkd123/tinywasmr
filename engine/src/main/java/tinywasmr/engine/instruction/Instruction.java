package tinywasmr.engine.instruction;

import tinywasmr.engine.execution.context.ExecutionContext;

@FunctionalInterface
public interface Instruction {
	public void execute(ExecutionContext ctx);
}
