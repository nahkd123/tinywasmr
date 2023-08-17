package tinywasmr.engine.instruction.impl;

import java.util.function.Consumer;

import tinywasmr.engine.instruction.InstructionFactory;
import tinywasmr.engine.instruction.param.I32InstructionFactory;

public class LocalInstructions {
	public static final I32InstructionFactory GET = new I32InstructionFactory(0x20, "local.get", (i32a, ctx) -> {
		ctx.getStack().push(ctx.getLocalOrTrap(i32a));
	});

	public static final I32InstructionFactory SET = new I32InstructionFactory(0x21, "local.set", (i32a, ctx) -> {
		var local = ctx.getLocalOrTrap(i32a);
		ctx.getStack().pop().copyTo(local);
	});

	public static final I32InstructionFactory TEE = new I32InstructionFactory(0x22, "local.tee", (i32a, ctx) -> {
		var local = ctx.getLocalOrTrap(i32a);
		var top = ctx.getStack().pop();
		top.copyTo(local);
		ctx.getStack().push(top);
	});

	public static void bootstrap(Consumer<InstructionFactory> consumer) {
		consumer.accept(GET);
		consumer.accept(SET);
		consumer.accept(TEE);
	}
}
