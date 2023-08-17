package tinywasmr.engine.instruction.impl;

import java.util.function.Consumer;

import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.instruction.InstructionFactory;
import tinywasmr.engine.instruction.param.NoParamsInstruction;

public class LogicInstructions {
	public static final NoParamsInstruction UNREACHABLE = new NoParamsInstruction(0x00, "unreachable", ctx -> {
		throw new TrapException();
	});
	public static final NoParamsInstruction NOP = new NoParamsInstruction(0x01, "nop", null);
	public static final NoParamsInstruction END = new NoParamsInstruction(0x0B, "end", ctx -> ctx.triggerReturn());
	public static final NoParamsInstruction RETURN = new NoParamsInstruction(0x0F, "return", ctx -> ctx
		.triggerReturn());
	public static final NoParamsInstruction DROP = new NoParamsInstruction(0x1A, "drop", ctx -> ctx.getStack().pop());

	public static void bootstrap(Consumer<InstructionFactory> consumer) {
		consumer.accept(UNREACHABLE);
		consumer.accept(NOP);
		consumer.accept(END);
		consumer.accept(RETURN);
		consumer.accept(DROP);
	}
}
