package tinywasmr.dbg;

import tinywasmr.engine.exec.value.FuncRefValue;
import tinywasmr.engine.exec.value.NumberF32Value;
import tinywasmr.engine.exec.value.NumberF64Value;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.value.NumberI64Value;
import tinywasmr.engine.exec.value.RefValue;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.value.Vector128Value;

public enum ValueDisplayMode {
	BIN {
		@Override
		public String asString(Value value, DebugSymbols symbols) {
			if (value instanceof RefValue ref) return refAsString(ref, symbols);
			if (value instanceof NumberI32Value i32) return "%32s".formatted(Integer.toBinaryString(i32.i32()));
			if (value instanceof NumberI64Value i64) return "%64s".formatted(Long.toBinaryString(i64.i64()));
			if (value instanceof NumberF32Value f32) return "%32s"
				.formatted(Integer.toBinaryString(Float.floatToRawIntBits(f32.f32())));
			if (value instanceof NumberF64Value f64) return "%64s"
				.formatted(Long.toBinaryString(Double.doubleToRawLongBits(f64.f64())));
			if (value instanceof Vector128Value v128) return "%64s%64s".formatted(
				Long.toBinaryString(v128.msb()),
				Long.toBinaryString(v128.lsb())).replaceAll(" ", "0");
			return value.toString();
		}
	},
	DEC {
		@Override
		public String asString(Value value, DebugSymbols symbols) {
			if (value instanceof RefValue ref) return refAsString(ref, symbols);
			if (value instanceof NumberI32Value i32) return "%d".formatted(i32.i32());
			if (value instanceof NumberI64Value i64) return "%d".formatted(i64.i64());
			if (value instanceof NumberF32Value f32) return "%f".formatted(f32.f32());
			if (value instanceof NumberF64Value f64) return "%f".formatted(f64.f64());
			if (value instanceof Vector128Value v128) return "%d.%d".formatted(v128.msb(), v128.lsb());
			return value.toString();
		}
	},
	OCT {
		@Override
		public String asString(Value value, DebugSymbols symbols) {
			if (value instanceof RefValue ref) return refAsString(ref, symbols);
			if (value instanceof NumberI32Value i32) return "%011o".formatted(i32.i32());
			if (value instanceof NumberI64Value i64) return "%022o".formatted(i64.i64());
			if (value instanceof NumberF32Value f32) return "%011o".formatted(Float.floatToRawIntBits(f32.f32()));
			if (value instanceof NumberF64Value f64) return "%022o".formatted(Double.doubleToRawLongBits(f64.f64()));
			if (value instanceof Vector128Value v128) return "%022o.%022o".formatted(v128.msb(), v128.lsb());
			return value.toString();
		}
	},
	HEX {
		@Override
		public String asString(Value value, DebugSymbols symbols) {
			if (value instanceof RefValue ref) return refAsString(ref, symbols);
			if (value instanceof NumberI32Value i32) return "%08x".formatted(i32.i32());
			if (value instanceof NumberI64Value i64) return "%016x".formatted(i64.i64());
			if (value instanceof NumberF32Value f32) return "%08x".formatted(Float.floatToRawIntBits(f32.f32()));
			if (value instanceof NumberF64Value f64) return "%016x".formatted(Double.doubleToRawLongBits(f64.f64()));
			if (value instanceof Vector128Value v128) return "%016x%016x".formatted(v128.msb(), v128.lsb());
			return value.toString();
		}
	};

	public abstract String asString(Value value, DebugSymbols symbols);

	public static String refAsString(RefValue ref, DebugSymbols symbols) {
		Object val = ref.get();
		if (val == null) return "null";
		if (val instanceof FuncRefValue func) return symbols.nameOf(func.function().declaration());
		return "ofHashCode(0x%08x)".formatted(val.hashCode());
	}
}
