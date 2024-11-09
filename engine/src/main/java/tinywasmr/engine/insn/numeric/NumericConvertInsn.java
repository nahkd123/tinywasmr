package tinywasmr.engine.insn.numeric;

import java.math.BigInteger;
import java.util.function.UnaryOperator;

import tinywasmr.engine.exec.value.NumberF32Value;
import tinywasmr.engine.exec.value.NumberF64Value;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.value.NumberI64Value;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;

public enum NumericConvertInsn implements Instruction {
	I32_WRAP_I64(v -> new NumberI32Value(v.i32())),
	I32_TRUNC_F32_S(v -> new NumberI32Value(v.i32())),
	I32_TRUNC_F32_U(v -> new NumberI32Value(v.i32())), // TODO trap
	I32_TRUNC_F64_S(v -> new NumberI32Value(v.i32())),
	I32_TRUNC_F64_U(v -> new NumberI32Value(v.i32())), // TODO trap
	I64_EXTEND_I32_S(v -> new NumberI64Value(v.i64())),
	I64_EXTEND_I32_U(v -> new NumberI64Value(v.i64() & 0xFFFFFFFFL)),
	I64_TRUNC_F32_S(v -> new NumberI64Value(v.i64())),
	I64_TRUNC_F32_U(v -> new NumberI64Value(v.i64())), // TODO trap
	I64_TRUNC_F64_S(v -> new NumberI64Value(v.i64())),
	I64_TRUNC_F64_U(v -> new NumberI64Value(v.i64())), // TODO trap
	F32_CONVERT_I32_S(v -> new NumberF32Value(v.i32())),
	F32_CONVERT_I32_U(v -> new NumberF32Value(v.i32() & 0xFFFFFFFFL)),
	F32_CONVERT_I64_S(v -> new NumberF32Value(v.i64())),
	F32_CONVERT_I64_U(v -> new NumberF32Value(new BigInteger(Long.toUnsignedString(v.i64())).floatValue())),
	F32_DEMOTE_F64(v -> new NumberF32Value(v.f32())),
	F64_CONVERT_I32_S(v -> new NumberF64Value(v.i32())),
	F64_CONVERT_I32_U(v -> new NumberF64Value(v.i32() & 0xFFFFFFFFL)),
	F64_CONVERT_I64_S(v -> new NumberF64Value(v.i64())),
	F64_CONVERT_I64_U(v -> new NumberF64Value(new BigInteger(Long.toUnsignedString(v.i64())).doubleValue())),
	F64_PROMOTE_F32(v -> new NumberF64Value(v.f64())),
	I32_REINTERPRET_F32(v -> new NumberI32Value(Float.floatToRawIntBits(v.f32()))),
	F32_REINTERPRET_I32(v -> new NumberF32Value(Float.intBitsToFloat(v.i32()))),
	I64_REINTERPRET_F64(v -> new NumberI64Value(Double.doubleToRawLongBits(v.f64()))),
	F64_REINTERPRET_I64(v -> new NumberF64Value(Double.longBitsToDouble(v.i64()))),
	;

	private UnaryOperator<Value> converter;

	private NumericConvertInsn(UnaryOperator<Value> converter) {
		this.converter = converter;
	}

	@Override
	public void execute(Machine vm) {
		Value val = vm.peekFrame().popOprand();
		val = converter.apply(val);
		vm.peekFrame().pushOperand(val);
	}
}
