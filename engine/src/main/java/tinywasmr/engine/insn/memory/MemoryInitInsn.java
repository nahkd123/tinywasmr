package tinywasmr.engine.insn.memory;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.memory.DataSegment;
import tinywasmr.engine.module.memory.MemoryDecl;

public record MemoryInitInsn(DataSegment segment, MemoryDecl memory) implements Instruction {
	@Override
	public void execute(Machine vm) {
		int count = vm.peekFrame().popOprand().i32();
		int dataOffset = vm.peekFrame().popOprand().i32();
		int memOffset = vm.peekFrame().popOprand().i32();
		execute(vm, segment, dataOffset, memory, memOffset, count);
	}

	public static void execute(Machine vm, DataSegment segment, int dataOffset, MemoryDecl memoryDecl, int memOffset, int count) {
		Memory memory = vm.peekInstancedFrame().getInstance().memory(memoryDecl);
		memory.write(memOffset, segment.data(), dataOffset, count);
	}
}
