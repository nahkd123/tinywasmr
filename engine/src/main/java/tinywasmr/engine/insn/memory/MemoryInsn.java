package tinywasmr.engine.insn.memory;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.memory.MemoryDecl;

public record MemoryInsn(MemoryInsnType type, MemoryDecl memory) implements Instruction {
	@Override
	public void execute(Machine vm) {
		Memory memory = vm.peekInstancedFrame().getInstance().memory(this.memory);
		type.execute(vm, memory);
	}
}
