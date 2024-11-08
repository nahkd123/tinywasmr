package tinywasmr.engine.insn.memory;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.value.NumberF32Value;
import tinywasmr.engine.exec.value.NumberF64Value;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.value.NumberI64Value;
import tinywasmr.engine.exec.value.Value;

public enum LoadType {
	I32((m, a) -> new NumberI32Value(m.readI32(a))),
	I64((m, a) -> new NumberI64Value(m.readI64(a))),
	F32((m, a) -> new NumberF32Value(m.readF32(a))),
	F64((m, a) -> new NumberF64Value(m.readF64(a))),
	I32_S8((m, a) -> new NumberI32Value(m.readS8(a))),
	I32_U8((m, a) -> new NumberI32Value(m.readU8(a))),
	I32_S16((m, a) -> new NumberI32Value(m.readS16(a))),
	I32_U16((m, a) -> new NumberI32Value(m.readU16(a))),
	I64_S8((m, a) -> new NumberI64Value(m.readS8(a))),
	I64_U8((m, a) -> new NumberI64Value(m.readU8(a))),
	I64_S16((m, a) -> new NumberI64Value(m.readS16(a))),
	I64_U16((m, a) -> new NumberI64Value(m.readU16(a))),
	I64_S32((m, a) -> new NumberI64Value(m.readS32(a))),
	I64_U32((m, a) -> new NumberI64Value(m.readU32(a))),
	;

	private Load loader;

	private LoadType(Load loader) {
		this.loader = loader;
	}

	public Value execute(Memory memory, int address) {
		return loader.load(memory, address);
	}

	@FunctionalInterface
	private interface Load {
		Value load(Memory memory, int address);
	}
}
