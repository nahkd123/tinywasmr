package tinywasmr.engine.insn.memory;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.memory.MemoryDecl;

public record StoreInsn(MemoryDecl memory, StoreType type, MemoryArg memarg) implements Instruction {
	@Override
	public void execute(Machine vm) {
		Value val = vm.peekFrame().popOprand();
		int address = vm.peekFrame().popOprand().i32();
		Memory memory = vm.peekInstancedFrame().getInstance().memory(this.memory);
		type.execute(memory, address, val);
	}
}
