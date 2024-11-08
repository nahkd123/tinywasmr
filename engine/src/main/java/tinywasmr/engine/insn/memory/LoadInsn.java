package tinywasmr.engine.insn.memory;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.memory.MemoryDecl;

public record LoadInsn(MemoryDecl memory, LoadType type, MemoryArg memarg) implements Instruction {
	@Override
	public void execute(Machine vm) {
		int address = vm.peekFrame().popOprand().i32();
		Memory memory = vm.peekFunctionFrame().getInstance().memory(this.memory);
		vm.peekFrame().pushOperand(type.execute(memory, address));
	}
}
