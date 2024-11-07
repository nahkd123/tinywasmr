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

public enum NumericUnaryOpInsn implements Instruction {
	I32_CLZ(Integer::numberOfLeadingZeros),
	I32_CTZ(Integer::numberOfTrailingZeros),
	I32_POPCNT(Integer::bitCount),
	I32_EQZ((int a) -> a == 0),

	I64_CLZ((long a) -> Long.numberOfLeadingZeros(a)),
	I64_CTZ((long a) -> Long.numberOfTrailingZeros(a)),
	I64_POPCNT((long a) -> Long.bitCount(a)),
	I64_EQZ((long a) -> a == 0L),

	F32_ABS((F32UnaryOp) Math::abs),
	F32_NEG((float a) -> -a),
	F32_SQRT((float a) -> (float) Math.sqrt(a)),
	F32_CEIL((float a) -> (float) Math.ceil(a)),
	F32_FLOOR((float a) -> (float) Math.floor(a)),
	F32_TRUNC((F32UnaryOp) a -> (long) a),
	F32_NEAREST((F32UnaryOp) Math::round),

	F64_ABS((F64UnaryOp) Math::abs),
	F64_NEG((double a) -> -a),
	F64_SQRT(Math::sqrt),
	F64_CEIL(Math::ceil),
	F64_FLOOR(Math::floor),
	F64_TRUNC((F64UnaryOp) a -> (long) a),
	F64_NEAREST((F64UnaryOp) Math::round),
	;

	private ValueType type;
	private UnaryOp operator;

	private NumericUnaryOpInsn(ValueType type, UnaryOp operator) {
		this.type = type;
		this.operator = operator;
	}

	private NumericUnaryOpInsn(I32UnaryOp operator) {
		this(NumberType.I32, a -> new NumberI32Value(operator.apply(a.i32())));
	}

	private NumericUnaryOpInsn(I32TestOp operator) {
		this(NumberType.I32, a -> operator.apply(a.i32()) ? Value.TRUE : Value.FALSE);
	}

	private NumericUnaryOpInsn(I64UnaryOp operator) {
		this(NumberType.I64, a -> new NumberI64Value(operator.apply(a.i64())));
	}

	private NumericUnaryOpInsn(I64TestOp operator) {
		this(NumberType.I64, a -> operator.apply(a.i64()) ? Value.TRUE : Value.FALSE);
	}

	private NumericUnaryOpInsn(F32UnaryOp operator) {
		this(NumberType.F32, a -> new NumberF32Value(operator.apply(a.f32())));
	}

	private NumericUnaryOpInsn(F64UnaryOp operator) {
		this(NumberType.F64, a -> new NumberF64Value(operator.apply(a.f64())));
	}

	@FunctionalInterface
	private interface UnaryOp {
		Value apply(Value input);
	}

	@FunctionalInterface
	private interface I32UnaryOp {
		int apply(int input);
	}

	@FunctionalInterface
	private interface I32TestOp {
		boolean apply(int input);
	}

	@FunctionalInterface
	private interface I64UnaryOp {
		long apply(long input);
	}

	@FunctionalInterface
	private interface I64TestOp {
		boolean apply(long input);
	}

	@FunctionalInterface
	private interface F32UnaryOp {
		float apply(float input);
	}

	@FunctionalInterface
	private interface F64UnaryOp {
		double apply(double input);
	}

	@Override
	public void execute(Machine vm) {
		Value value = vm.peekFrame().popOprand();
		if (vm.hasRuntimeValidation() && !value.type().equals(type))
			throw new ValidationException("Expected %s on stack, found %s".formatted(type, value.type()));
		vm.peekFrame().pushOperand(operator.apply(value));
	}
}
