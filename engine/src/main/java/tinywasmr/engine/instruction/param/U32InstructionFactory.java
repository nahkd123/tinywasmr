package tinywasmr.engine.instruction.param;

import java.io.IOException;

import tinywasmr.engine.execution.context.ExecutionContext;
import tinywasmr.engine.instruction.FixedOpcodeInstructionFactory;
import tinywasmr.engine.instruction.Instruction;
import tinywasmr.engine.io.LEDataInput;

public class U32InstructionFactory implements FixedOpcodeInstructionFactory {
	@FunctionalInterface
	public static interface I32Executor {
		public void execute(int i32a, ExecutionContext ctx);
	}

	private static class I32Instruction implements Instruction {
		private int i32a;
		private String name;
		private I32Executor executor;

		public I32Instruction(int i32a, String name, I32Executor executor) {
			this.i32a = i32a;
			this.name = name;
			this.executor = executor;
		}

		@Override
		public void execute(ExecutionContext ctx) {
			executor.execute(i32a, ctx);
		}

		@Override
		public String toString() {
			return name + " " + i32a;
		}
	}

	private int opcode;
	private String name;
	private I32Executor executor;

	public U32InstructionFactory(int opcode, String name, I32Executor executor) {
		this.opcode = opcode;
		this.name = name;
		this.executor = executor;
	}

	@Override
	public int getOpcode() { return opcode; }

	@Override
	public Instruction parse(LEDataInput in) throws IOException {
		return new I32Instruction((int) in.readLEB128Unsigned(), name, executor);
	}

	public Instruction create(int i32a) {
		return new I32Instruction(i32a, name, executor);
	}
}
