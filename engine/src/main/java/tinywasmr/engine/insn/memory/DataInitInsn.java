package tinywasmr.engine.insn.memory;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.memory.DataSegment;
import tinywasmr.engine.module.memory.MemoryDecl;

public record DataInitInsn(DataSegment segment, MemoryDecl memory) implements Instruction {
	@Override
	public void execute(Machine vm) {
		int count = vm.peekFrame().popOprand().i32();
		int dataOffset = vm.peekFrame().popOprand().i32();
		int memOffset = vm.peekFrame().popOprand().i32();
		Memory memory = vm.peekFunctionFrame().getInstance().memory(this.memory);
		memory.write(memOffset, segment.data(), dataOffset, count);
	}
}
