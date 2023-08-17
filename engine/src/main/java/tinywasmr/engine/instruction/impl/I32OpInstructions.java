package tinywasmr.engine.instruction.impl;

import java.util.function.Consumer;

import tinywasmr.engine.instruction.InstructionFactory;
import tinywasmr.engine.instruction.param.NoParamsInstruction;

public class I32OpInstructions {
	@FunctionalInterface
	private static interface Int2IntFunction {
		public int apply(int v);
	}

	@FunctionalInterface
	private static interface IntInt2IntFunction {
		public int apply(int a, int b);
	}

	private static NoParamsInstruction opI2I(int opcode, String name, Int2IntFunction f) {
		return new NoParamsInstruction(opcode, "i32." + name, ctx -> {
			int a = ctx.getStack().popI32();
			a = f.apply(a);
			ctx.getStack().pushI32(a);
		});
	}

	private static NoParamsInstruction opII2I(int opcode, String name, IntInt2IntFunction f) {
		return new NoParamsInstruction(opcode, "i32." + name, ctx -> {
			int b = ctx.getStack().popI32();
			int a = ctx.getStack().popI32();
			a = f.apply(a, b);
			ctx.getStack().pushI32(a);
		});
	}

	public static final NoParamsInstruction EQZ = opI2I(0x45, "eqz", v -> v == 0 ? 1 : 0);
	public static final NoParamsInstruction EQ = opII2I(0x46, "eq", (a, b) -> a == b ? 1 : 0);
	public static final NoParamsInstruction NE = opII2I(0x47, "ne", (a, b) -> a != b ? 1 : 0);
	public static final NoParamsInstruction LT_S = opII2I(0x48, "lt_s", (a, b) -> a < b ? 1 : 0);
	public static final NoParamsInstruction LT_U = opII2I(0x49, "lt_u",
		(a, b) -> Integer.compareUnsigned(a, b) < 0 ? 1 : 0);
	public static final NoParamsInstruction GT_S = opII2I(0x4A, "gt_s", (a, b) -> a > b ? 1 : 0);
	public static final NoParamsInstruction GT_U = opII2I(0x4B, "gt_u",
		(a, b) -> Integer.compareUnsigned(a, b) > 0 ? 1 : 0);

	public static final NoParamsInstruction LE_S = opII2I(0x4C, "le_s", (a, b) -> a <= b ? 1 : 0);
	public static final NoParamsInstruction LE_U = opII2I(0x4D, "le_u",
		(a, b) -> Integer.compareUnsigned(a, b) <= 0 ? 1 : 0);
	public static final NoParamsInstruction GE_S = opII2I(0x4E, "ge_s", (a, b) -> a >= b ? 1 : 0);
	public static final NoParamsInstruction GE_U = opII2I(0x4F, "ge_u",
		(a, b) -> Integer.compareUnsigned(a, b) >= 0 ? 1 : 0);

	// TODO CLZ
	// TODO CTZ
	// TODO POPCNT

	public static final NoParamsInstruction ADD = opII2I(0x6A, "add", (a, b) -> a + b);
	public static final NoParamsInstruction SUB = opII2I(0x6A, "sub", (a, b) -> a - b);
	public static final NoParamsInstruction MUL = opII2I(0x6A, "mul", (a, b) -> a * b);
	public static final NoParamsInstruction DIV_S = opII2I(0x6A, "div_s", (a, b) -> a / b);
	public static final NoParamsInstruction DIV_U = opII2I(0x6A, "div_u", (a, b) -> Integer.divideUnsigned(a, b));
	public static final NoParamsInstruction REM_S = opII2I(0x6A, "rem_s", (a, b) -> a % b);
	public static final NoParamsInstruction REM_U = opII2I(0x6A, "rem_u", (a, b) -> Integer.remainderUnsigned(a, b));

	public static void bootstrap(Consumer<InstructionFactory> consumer) {
		consumer.accept(EQZ);
		consumer.accept(EQ);
		consumer.accept(NE);
		consumer.accept(LT_S);
		consumer.accept(LT_U);
		consumer.accept(GT_S);
		consumer.accept(GT_U);
		consumer.accept(LE_S);
		consumer.accept(LE_U);
		consumer.accept(GE_S);
		consumer.accept(GE_U);

		consumer.accept(ADD);
		consumer.accept(SUB);
		consumer.accept(MUL);
		consumer.accept(DIV_S);
		consumer.accept(DIV_U);
		consumer.accept(REM_S);
		consumer.accept(REM_U);
	}
}
