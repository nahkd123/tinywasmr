package tinywasmr.engine.insn.numeric;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.value.NumberF32Value;
import tinywasmr.engine.exec.value.NumberF64Value;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.value.NumberI64Value;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.ValueType;

public enum NumericBinaryOpInsn implements Instruction {
	I32_EQ((int a, int b) -> a == b),
	I32_NE((int a, int b) -> a != b),
	I32_LT_S((int a, int b) -> a < b),
	I32_LT_U((int a, int b) -> Integer.compareUnsigned(a, b) < 0),
	I32_GT_S((int a, int b) -> a > b),
	I32_GT_U((int a, int b) -> Integer.compareUnsigned(a, b) > 0),
	I32_LE_S((int a, int b) -> a <= b),
	I32_LE_U((int a, int b) -> Integer.compareUnsigned(a, b) <= 0),
	I32_GE_S((int a, int b) -> a >= b),
	I32_GE_U((int a, int b) -> Integer.compareUnsigned(a, b) >= 0),
	I32_ADD((int a, int b) -> a + b),
	I32_SUB((int a, int b) -> a - b),
	I32_MUL((int a, int b) -> a * b),
	I32_DIV_S((int a, int b) -> a / b),
	I32_DIV_U(Integer::divideUnsigned),
	I32_REM_S((int a, int b) -> a % b),
	I32_REM_U(Integer::remainderUnsigned),
	I32_AND((int a, int b) -> a & b),
	I32_OR((int a, int b) -> a | b),
	I32_XOR((int a, int b) -> a ^ b),
	I32_SHL((int a, int b) -> a << b),
	I32_SHR_S((int a, int b) -> a >> b),
	I32_SHR_U((int a, int b) -> a >>> b),
	I32_ROTL(Integer::rotateLeft),
	I32_ROTR(Integer::rotateRight),

	I64_EQ((long a, long b) -> a == b),
	I64_NE((long a, long b) -> a != b),
	I64_LT_S((long a, long b) -> a < b),
	I64_LT_U((long a, long b) -> Long.compareUnsigned(a, b) < 0),
	I64_GT_S((long a, long b) -> a > b),
	I64_GT_U((long a, long b) -> Long.compareUnsigned(a, b) > 0),
	I64_LE_S((long a, long b) -> a <= b),
	I64_LE_U((long a, long b) -> Long.compareUnsigned(a, b) <= 0),
	I64_GE_S((long a, long b) -> a >= b),
	I64_GE_U((long a, long b) -> Long.compareUnsigned(a, b) >= 0),
	I64_ADD((long a, long b) -> a + b),
	I64_SUB((long a, long b) -> a - b),
	I64_MUL((long a, long b) -> a * b),
	I64_DIV_S((long a, long b) -> a / b),
	I64_DIV_U(Long::divideUnsigned),
	I64_REM_S((long a, long b) -> a % b),
	I64_REM_U(Long::remainderUnsigned),
	I64_AND((long a, long b) -> a & b),
	I64_OR((long a, long b) -> a | b),
	I64_XOR((long a, long b) -> a ^ b),
	I64_SHL((long a, long b) -> a << b),
	I64_SHR_S((long a, long b) -> a >> b),
	I64_SHR_U((long a, long b) -> a >>> b),
	I64_ROTL((long a, long b) -> Long.rotateLeft(a, (int) b)),
	I64_ROTR((long a, long b) -> Long.rotateRight(a, (int) b)),

	F32_EQ((float a, float b) -> a == b),
	F32_NE((float a, float b) -> a != b),
	F32_LT((float a, float b) -> a < b),
	F32_GT((float a, float b) -> a > b),
	F32_LE((float a, float b) -> a <= b),
	F32_GE((float a, float b) -> a >= b),
	F32_ADD((float a, float b) -> a + b),
	F32_SUB((float a, float b) -> a - b),
	F32_MUL((float a, float b) -> a * b),
	F32_DIV((float a, float b) -> a / b),
	F32_MIN((F32BinaryOp) Math::min),
	F32_MAX((F32BinaryOp) Math::max),
	F32_COPYSIGN((float a, float b) -> Math.abs(a) * Math.signum(b)),

	F64_EQ((double a, double b) -> a == b),
	F64_NE((double a, double b) -> a != b),
	F64_LT((double a, double b) -> a < b),
	F64_GT((double a, double b) -> a > b),
	F64_LE((double a, double b) -> a <= b),
	F64_GE((double a, double b) -> a >= b),
	F64_ADD((double a, double b) -> a + b),
	F64_SUB((double a, double b) -> a - b),
	F64_MUL((double a, double b) -> a * b),
	F64_DIV((double a, double b) -> a / b),
	F64_MIN((F64BinaryOp) Math::min),
	F64_MAX((F64BinaryOp) Math::max),
	F64_COPYSIGN((double a, double b) -> Math.abs(a) * Math.signum(b)),
	;

	private ValueType bottomType;
	private ValueType topType;
	private BinaryOp operator;

	private NumericBinaryOpInsn(ValueType bottomType, ValueType topType, BinaryOp operator) {
		this.bottomType = bottomType;
		this.topType = topType;
		this.operator = operator;
	}

	private NumericBinaryOpInsn(ValueType type, BinaryOp operator) {
		this(type, type, operator);
	}

	private NumericBinaryOpInsn(I32BinaryOp operator) {
		this(NumberType.I32, (a, b) -> new NumberI32Value(operator.apply(a.i32(), b.i32())));
	}

	private NumericBinaryOpInsn(I32CmpOp operator) {
		this(NumberType.I32, (a, b) -> operator.apply(a.i32(), b.i32()) ? Value.TRUE : Value.FALSE);
	}

	private NumericBinaryOpInsn(I64BinaryOp operator) {
		this(NumberType.I64, (a, b) -> new NumberI64Value(operator.apply(a.i64(), b.i64())));
	}

	private NumericBinaryOpInsn(I64CmpOp operator) {
		this(NumberType.I64, (a, b) -> operator.apply(a.i64(), b.i64()) ? Value.TRUE : Value.FALSE);
	}

	private NumericBinaryOpInsn(F32BinaryOp operator) {
		this(NumberType.F32, (a, b) -> new NumberF32Value(operator.apply(a.f32(), b.f32())));
	}

	private NumericBinaryOpInsn(F32CmpOp operator) {
		this(NumberType.F32, (a, b) -> operator.apply(a.f32(), b.f32()) ? Value.TRUE : Value.FALSE);
	}

	private NumericBinaryOpInsn(F64BinaryOp operator) {
		this(NumberType.F64, (a, b) -> new NumberF64Value(operator.apply(a.f64(), b.f64())));
	}

	private NumericBinaryOpInsn(F64CmpOp operator) {
		this(NumberType.F64, (a, b) -> operator.apply(a.f64(), b.f64()) ? Value.TRUE : Value.FALSE);
	}

	@FunctionalInterface
	private interface BinaryOp {
		Value apply(Value bottom, Value top);
	}

	@FunctionalInterface
	private interface I32BinaryOp {
		int apply(int bottom, int top);
	}

	@FunctionalInterface
	private interface I32CmpOp {
		boolean apply(int bottom, int top);
	}

	@FunctionalInterface
	private interface I64BinaryOp {
		long apply(long bottom, long top);
	}

	@FunctionalInterface
	private interface I64CmpOp {
		boolean apply(long bottom, long top);
	}

	@FunctionalInterface
	private interface F32BinaryOp {
		float apply(float bottom, float top);
	}

	@FunctionalInterface
	private interface F32CmpOp {
		boolean apply(float bottom, float top);
	}

	@FunctionalInterface
	private interface F64BinaryOp {
		double apply(double bottom, double top);
	}

	@FunctionalInterface
	private interface F64CmpOp {
		boolean apply(double bottom, double top);
	}

	public Value apply(Value a, Value b) {
		return operator.apply(a, b);
	}

	@Override
	public void execute(Machine vm) {
		Value top = vm.peekFrame().popOprand();
		Value bottom = vm.peekFrame().popOprand();

		if (vm.hasRuntimeValidation()) {
			if (!top.type().equals(topType)) throw new ValidationException("Expected %s at top, found %s"
				.formatted(topType, top.type()));
			if (!bottom.type().equals(bottomType)) throw new ValidationException("Expected %s at bottom, found %s"
				.formatted(topType, bottom.type()));
		}

		vm.peekFrame().pushOperand(operator.apply(bottom, top));
	}
}
