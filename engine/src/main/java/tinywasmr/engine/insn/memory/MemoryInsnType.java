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
	},
	COPY {
		@Override
		public void execute(Machine vm, Memory memory) {
			int count = vm.peekFrame().popOprand().i32();
			int sourceAddress = vm.peekFrame().popOprand().i32();
			int destAddress = vm.peekFrame().popOprand().i32();
			byte[] clipboard = memory.read(sourceAddress, count);
			memory.write(destAddress, clipboard);
			// TODO more efficient data copy mechanism
		}
	},
	FILL {
		@Override
		public void execute(Machine vm, Memory memory) {
			int count = vm.peekFrame().popOprand().i32();
			int bval = vm.peekFrame().popOprand().i32();
			int memOffset = vm.peekFrame().popOprand().i32();
			memory.fill(memOffset, bval, count);
		}
	};

	public abstract void execute(Machine vm, Memory memory);
}
