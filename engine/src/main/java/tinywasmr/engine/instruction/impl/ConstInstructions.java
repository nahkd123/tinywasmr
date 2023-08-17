package tinywasmr.engine.instruction.impl;

import java.util.function.Consumer;

import tinywasmr.engine.instruction.InstructionFactory;
import tinywasmr.engine.instruction.param.S32InstructionFactory;

public class ConstInstructions {
	public static final S32InstructionFactory I32_CONST = new S32InstructionFactory(0x41, "i32.const", (i32a, ctx) -> {
		ctx.getStack().pushI32(i32a);
	});

	public static void bootstrap(Consumer<InstructionFactory> consumer) {
		consumer.accept(I32_CONST);
	}
}
