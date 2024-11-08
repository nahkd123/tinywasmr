package tinywasmr.engine.insn.memory;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.value.Value;

public enum StoreType {
	I32((m, a, v) -> m.writeI32(a, v.i32())),
	I64((m, a, v) -> m.writeI64(a, v.i64())),
	F32((m, a, v) -> m.writeF32(a, v.f32())),
	F64((m, a, v) -> m.writeF64(a, v.f64())),
	I32_I8((m, a, v) -> m.writeI8(a, v.i32())),
	I32_I16((m, a, v) -> m.writeI16(a, v.i32())),
	I64_I8((m, a, v) -> m.writeI8(a, (int) v.i64())),
	I64_I16((m, a, v) -> m.writeI16(a, (int) v.i64())),
	I64_I32((m, a, v) -> m.writeI32(a, (int) v.i64())),
	;

	private Store storer;

	private StoreType(Store storer) {
		this.storer = storer;
	}

	public void execute(Memory memory, int address, Value value) {
		storer.store(memory, address, value);
	}

	@FunctionalInterface
	private interface Store {
		void store(Memory memory, int address, Value value);
	}
}
