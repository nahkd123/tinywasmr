package tinywasmr.engine.insn.memory;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.vm.Machine;

public enum MemoryInsnType {
	SIZE {
		@Override
		public void execute(Machine vm, Memory memory) {
			vm.peekFrame().pushOperand(new NumberI32Value(memory.pageCount()));
		}
	},
	GROW {
		@Override
		public void execute(Machine vm, Memory memory) {
			int delta = vm.peekFrame().popOprand().i32();
			vm.peekFrame().pushOperand(new NumberI32Value(memory.grow(delta)));
		}
	};

	public abstract void execute(Machine vm, Memory memory);
}
