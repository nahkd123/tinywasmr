package tinywasmr.engine.instruction;

public final class Instructions {
	// https://webassembly.github.io/spec/core/appendix/index-instructions.html

	public static final int UNREACHABLE = 0x00;
	public static final int NOP = 0x01;
	public static final int END = 0x0B;

	public static final int RETURN = 0x0F;

	public static final int DROP = 0x1A;

	public static final int LOCAL_GET = 0x20;
	public static final int LOCAL_SET = 0x21;
	public static final int LOCAL_TEE = 0x22;

	public static final int I32_CONST = 0x41;
	public static final int I64_CONST = 0x42;
	public static final int F32_CONST = 0x43;
	public static final int F64_CONST = 0x44;

	public static final int I32_ADD = 0x6A;

	public static final int F32_ADD = 0x92;
}
