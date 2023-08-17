package tinywasmr.engine.instruction.impl;

import static java.lang.Integer.compareUnsigned;
import static java.lang.Integer.divideUnsigned;
import static java.lang.Integer.numberOfLeadingZeros;
import static java.lang.Integer.numberOfTrailingZeros;
import static java.lang.Integer.remainderUnsigned;
import static java.lang.Integer.rotateLeft;
import static java.lang.Integer.rotateRight;

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
	public static final NoParamsInstruction LT_U = opII2I(0x49, "lt_u", (a, b) -> compareUnsigned(a, b) < 0 ? 1 : 0);
	public static final NoParamsInstruction GT_S = opII2I(0x4A, "gt_s", (a, b) -> a > b ? 1 : 0);
	public static final NoParamsInstruction GT_U = opII2I(0x4B, "gt_u", (a, b) -> compareUnsigned(a, b) > 0 ? 1 : 0);
	public static final NoParamsInstruction LE_S = opII2I(0x4C, "le_s", (a, b) -> a <= b ? 1 : 0);
	public static final NoParamsInstruction LE_U = opII2I(0x4D, "le_u", (a, b) -> compareUnsigned(a, b) <= 0 ? 1 : 0);
	public static final NoParamsInstruction GE_S = opII2I(0x4E, "ge_s", (a, b) -> a >= b ? 1 : 0);
	public static final NoParamsInstruction GE_U = opII2I(0x4F, "ge_u", (a, b) -> compareUnsigned(a, b) >= 0 ? 1 : 0);
	public static final NoParamsInstruction CLZ = opI2I(0x67, "clz", v -> numberOfLeadingZeros(v));
	public static final NoParamsInstruction CTZ = opI2I(0x67, "ctz", v -> numberOfTrailingZeros(v));

	// TODO POPCNT

	public static final NoParamsInstruction ADD = opII2I(0x6A, "add", (a, b) -> a + b);
	public static final NoParamsInstruction SUB = opII2I(0x6B, "sub", (a, b) -> a - b);
	public static final NoParamsInstruction MUL = opII2I(0x6C, "mul", (a, b) -> a * b);
	public static final NoParamsInstruction DIV_S = opII2I(0x6D, "div_s", (a, b) -> a / b);
	public static final NoParamsInstruction DIV_U = opII2I(0x6E, "div_u", (a, b) -> divideUnsigned(a, b));
	public static final NoParamsInstruction REM_S = opII2I(0x6F, "rem_s", (a, b) -> a % b);
	public static final NoParamsInstruction REM_U = opII2I(0x70, "rem_u", (a, b) -> remainderUnsigned(a, b));
	public static final NoParamsInstruction AND = opII2I(0x71, "and", (a, b) -> a & b);
	public static final NoParamsInstruction OR = opII2I(0x72, "or", (a, b) -> a | b);
	public static final NoParamsInstruction XOR = opII2I(0x73, "xor", (a, b) -> a ^ b);
	public static final NoParamsInstruction SHL = opII2I(0x74, "shl", (a, b) -> a << b);
	public static final NoParamsInstruction SHR_S = opII2I(0x75, "shr_s", (a, b) -> a >> b);
	public static final NoParamsInstruction SHR_U = opII2I(0x76, "shr_u", (a, b) -> a >>> b);
	public static final NoParamsInstruction ROT_L = opII2I(0x77, "rot_l", (a, b) -> rotateLeft(a, b));
	public static final NoParamsInstruction ROT_R = opII2I(0x78, "rot_r", (a, b) -> rotateRight(a, b));

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
		consumer.accept(CLZ);
		consumer.accept(CTZ);
		// TODO
		consumer.accept(ADD);
		consumer.accept(SUB);
		consumer.accept(MUL);
		consumer.accept(DIV_S);
		consumer.accept(DIV_U);
		consumer.accept(REM_S);
		consumer.accept(REM_U);
		consumer.accept(AND);
		consumer.accept(OR);
		consumer.accept(XOR);
		consumer.accept(SHL);
		consumer.accept(SHR_S);
		consumer.accept(SHR_U);
		consumer.accept(ROT_L);
		consumer.accept(ROT_R);
	}
}
