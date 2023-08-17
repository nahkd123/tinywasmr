package tinywasmr.engine.instruction.param;

import java.io.IOException;
import java.util.function.Consumer;

import tinywasmr.engine.execution.ExecutionContext;
import tinywasmr.engine.instruction.FixedOpcodeInstructionFactory;
import tinywasmr.engine.instruction.Instruction;
import tinywasmr.engine.io.LEDataInput;

public class NoParamsInstruction implements Instruction, FixedOpcodeInstructionFactory {
	private int opcode;
	private String name;
	private Consumer<ExecutionContext> executor;

	public NoParamsInstruction(int opcode, String name, Consumer<ExecutionContext> executor) {
		this.opcode = opcode;
		this.name = name;
		this.executor = executor;
	}

	@Override
	public int getOpcode() { return opcode; }

	@Override
	public void execute(ExecutionContext ctx) {
		if (executor != null) executor.accept(ctx);
	}

	@Override
	public Instruction parse(LEDataInput in) throws IOException {
		return this;
	}

	@Override
	public String toString() {
		return name;
	}
}
